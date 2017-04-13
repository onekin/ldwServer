package org.onekin.ldw.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.util.EnumerationIterator;

/**
 * Velocity dispatcher
 *
 * @author David Czarnecki
 * @since blojsom 3.0
 * @version $Id: VelocityDispatcher.java,v 1.6 2007/01/17 02:35:17 czarneckid Exp $
 */
public class VelocityDispatcher {

    private Log _logger = LogFactory.getLog(VelocityDispatcher.class);

    private static final String BLOJSOM_RENDER_TOOL = "BLOJSOM_RENDER_TOOL";

    private Properties _velocityProperties;
   
    /**
     * Create a new instance of the Velocity dispatcher
     */
    public VelocityDispatcher() {
    }

    /**
     * Initialization method for blojsom dispatchers
     *
     */
    public void init() {
        
    }

    /**
     * Set the Velocity properties for use by the dispatcher
     *
     * @param velocityProperties Properties for Velocity configuration
     */
    public void setVelocityProperties(Properties velocityProperties) {
        _velocityProperties = velocityProperties;
    }


    /**
     * Populate the Velocity context with the request and session attributes
     *
     * @param httpServletRequest Request
     * @param context            Context
     */
    protected void populateVelocityContext(HttpServletRequest httpServletRequest, Map context) {
        EnumerationIterator iterator = new EnumerationIterator(httpServletRequest.getAttributeNames());
        while (iterator.hasNext()) {
            Object key = iterator.next();
            Object value = httpServletRequest.getAttribute(key.toString());
            context.put(key, value);
        }

        HttpSession httpSession = httpServletRequest.getSession(false);
        if (httpSession != null) {
            iterator = new EnumerationIterator(httpSession.getAttributeNames());
            while (iterator.hasNext()) {
                Object key = iterator.next();
                Object value = httpSession.getAttribute(key.toString());
                context.put(key, value);
            }
        }
    }

    /**
     * Remove references from the Velocity context
     *
     * @param velocityContext {@link VelocityContext}
     */
    protected void destroyVelocityContext(VelocityContext velocityContext) {
        // Make sure no objects are referenced in the context after they're finished
        Object[] contextKeys = velocityContext.getKeys();
        for (int i = 0; i < contextKeys.length; i++) {
            Object contextKey = contextKeys[i];
            velocityContext.remove(contextKey);
        }
    }

    /**
     * Dispatch a request and response. A context map is provided for the BlojsomServlet to pass
     * any required information for use by the dispatcher. The dispatcher is also
     * provided with the template for the requested flavor along with the content type for the
     * specific flavor.
     *
     * @param httpServletRequest  Request
     * @param httpServletResponse Response
     * @param blog                {@link Blog}
     * @param context             Context map
     * @throws java.io.IOException            If there is an exception during IO
     * @throws javax.servlet.ServletException If there is an exception in dispatching the request
     */
    public void dispatch(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Map context) throws IOException, ServletException {
        httpServletResponse.setContentType("text/html");
        
        // Create the Velocity Engine
        VelocityEngine velocityEngine = new VelocityEngine();
        try {
            Properties updatedProperties = (Properties) _velocityProperties.clone();
           // updatedProperties.put(VelocityEngine.FILE_RESOURCE_LOADER_PATH, servletContext.getRealPath(BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _blogsDirectory + blog.getBlogId() + _templatesDirectory) + ", " + servletContext.getRealPath(BlojsomConstants.DEFAULT_CONFIGURATION_BASE_DIRECTORY + _templatesDirectory));
            velocityEngine.init(updatedProperties);
        } catch (Exception e) {
            if (_logger.isErrorEnabled()) {
                _logger.error(e);
            }
            return;
        }

        Writer responseWriter = httpServletResponse.getWriter();
        String flavorTemplateForPage = null;
        String pageParameter = Utils.getRequestValue(Cons.PAGE_PARAM, httpServletRequest, true);
        flavorTemplateForPage=pageParameter;
        /*
        if (pageParameter != null) {
            flavorTemplateForPage = BlojsomUtils.getTemplateForPage(flavorTemplate, pageParameter);

            if (_logger.isDebugEnabled()) {
                _logger.debug("Retrieved template for page: " + flavorTemplateForPage);
            }
        }
*/
        
        // Setup the VelocityContext
        populateVelocityContext(httpServletRequest, context);
        VelocityContext velocityContext = new VelocityContext(context);
        velocityContext.put(BLOJSOM_RENDER_TOOL, new BlojsomRenderTool(velocityEngine, velocityContext));

        if (flavorTemplateForPage != null) {
            // Try and look for the flavor page template for the individual user
            if (!velocityEngine.resourceExists(flavorTemplateForPage)) {
                if (_logger.isErrorEnabled()) {
                    _logger.error("Could not find flavor page template for user: " + flavorTemplateForPage);
                }

                responseWriter.flush();
                destroyVelocityContext(velocityContext);

                return;
            } else {
                try {
                    velocityEngine.mergeTemplate(flavorTemplateForPage, Cons.UTF8, velocityContext, responseWriter);
                } catch (Exception e) {
                    if (_logger.isErrorEnabled()) {
                        _logger.error(e);
                    }

                    responseWriter.flush();
                    destroyVelocityContext(velocityContext);

                    return;
                }
            }

            _logger.debug("Dispatched to flavor page template: " + flavorTemplateForPage);
        } else {
                if (_logger.isErrorEnabled()) {
                    _logger.error("Flavor don't provided." );
                }
        }

        responseWriter.flush();
        destroyVelocityContext(velocityContext);
    }

    /**
     * Blojsom render tool mimics the functionality of the Velocity render tool to parse VTL markup added to a
     * template
     */
    public class BlojsomRenderTool {

        private static final String LOG_TAG = "BlojsomRenderTool";

        private VelocityEngine _velocityEngine;
        private VelocityContext _velocityContext;

        /**
         * Create a new instance of the render tool
         *
         * @param velocityEngine  {@link VelocityEngine}
         * @param velocityContext {@link VelocityContext}
         */
        public BlojsomRenderTool(VelocityEngine velocityEngine, VelocityContext velocityContext) {
            _velocityEngine = velocityEngine;
            _velocityContext = velocityContext;
        }

        /**
         * Evaluate a string containing VTL markup
         *
         * @param template VTL markup
         * @return Processed VTL or <code>null</code> if an error in evaluation
         */
        public String evaluate(String template) {
            if (Utils.checkNullOrBlank(template)) {
                return null;
            }

            StringWriter sw = new StringWriter();
            boolean success = false;

            try {
                if (_velocityEngine == null) {
                    success = Velocity.evaluate(_velocityContext, sw, LOG_TAG, template);
                } else {
                    success = _velocityEngine.evaluate(_velocityContext, sw, LOG_TAG, template);
                }
            } catch (ParseErrorException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            } catch (MethodInvocationException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            } catch (ResourceNotFoundException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            } catch (IOException e) {
                if (_logger.isErrorEnabled()) {
                    _logger.error(e);
                }
            }

            if (success) {
                return sw.toString();
            }

            return null;
        }
    }
}

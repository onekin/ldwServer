YUI.add('dt-collapse', function(Y) {

    Y.Handlebars.registerHelper('cmp', function (val0, val1, options) {
        return (val0 == val1) ? options.fn(this) : options.inverse(this);
    });

    if (!DT) DT = {};

    var template = Y.one('#t1').getHTML();
    var _template = Y.one('#t').getHTML();

    DT.CollapseGroup = Y.Base.create('panel-group', Y.View, [], {
        containerTemplate: '<div class="panel"/>',
        template: Y.Handlebars.compile(template),
        _template: Y.Handlebars.compile(_template),
        events: {
            '.panel-heading' : {
                'click' : function(e) {
                    var container = this.get('container');
                    e.preventDefault();
                    e.stopPropagation();
                    var element = container.one('.panel-collapse'); // Y.one(e.target.getAttribute('href'));
                    this.set('element', element);
                    this[element.hasClass('in') ? 'hide' : 'show']();
                }
            }
        },
        initializer: function() {
            this.publish('expand:start');
            this.publish('expand:end');
            this.publish('collapse:start');
            this.publish('collapse:end');
            this.after('expand:start', function(e) {
                var item = this.get('item');
                this.setHTML(this._template(item));
            }, this);

            return this;
        },
        render: function() {
            var container = this.get('container');
            container.setHTML(this.template(this.get('item')));

            if (this.get('collapsed')) container.addClass('collapsed');
            return this;
        },
        show: function() {
            var view = this,
                container = this.get('container'),
                element = this.get('element'),
                actives, 
                hasData;

            if (this.get('transitioning')) return;

            view.fire('expand:start');

            container.removeClass('collapsed');

            element.setStyle('display', 'block')
                   .replaceClass('collapse', 'collapsing')
                   .setStyle('height', 0);

            this.set('transitioning', true);

            element.transition({
                height: element.get('scrollHeight') + 'px',
                duration: 0.5,
                on: {
                    start: function() {},
                    end: function() {
                        element.replaceClass('collapsing', 'in')
                               .setStyle('height', 'auto');
                        view.set('transitioning', false);
                        view.set('collapsed', false);
                        view.fire('expand:end');
                    }
                }
            });

        },
        hide: function() {
            var view = this,
                container = this.get('container'),
                element = this.get('element'),
                actives, 
                hasData;

            if (this.get('transitioning')) return;
            view.fire('hide:start');
            actives =  container && container.all('> .panel-group > .in');

            element.setStyle('height', element.getComputedStyle('height'))
                   .get('offsetHeight');
            element.removeClass('in')
                   .removeClass('collapse')
                   .addClass('collapsing')

            this.set('transitioning', true);

            element.transition({
                height: '0px',
                duration: 0.2,
                on: {
                    start: function() {},
                    end: function() {
                        element.replaceClass('collapsing', 'collapsed');
                        element.ancestor('.panel').addClass('collapsed');
                        element.setStyle('height', '0px');
                        element.setStyle('display', 'none');

                        view.set('transitioning', false);
                        view.fire('hide:end');
                        view.set('collapsed', true);
                    }
                }
            });
        },
        setHTML: function(html) {
            var container = this.get('container');
            container.one('.panel-body').setHTML(html);
            return true;
        },
        reset: function() {},
        transition: function() {},
        toggle: function() {}
    }, {
        ATTRS: {
            collapsed: {
                value: true
            },
            transitioning: {
                value: false
            }
        }
    });

    DT.Collapse = Y.Base.create('accordion', Y.View, [Y.EventTarget], {
        containerTemplate: '<div />',
        initializer: function() {
            this.after('listChange', function(e) {
                this.flush();
                e.newVal.each(function(item) {
                    if (item.name) this.add(item);
                }, this);
            }, this);
            return this;
        },
        render: function() {
            var container = this.get('container'),
                list = this.get('list');

            list.each(function(item) {
                if (item.name) this.add(item);
            }, this);

            return this;
        },
        add: function(item) {
            var groups = this.get('groups'),
                group = new DT.CollapseGroup({
                item: item
            }).render();

            group.addTarget(this);
            this.get('container').appendChild(group.get('container'));
            groups.push(group);
            this.set('groups', groups);
        },
        flush: function() {
            var groups = this.get('groups');
            Y.Array.each(groups, function(group) {
                group.destroy({ remove: true });
            });
            this.get('container').setHTML('');
        }
    }, {
        ATTRS: {
            items: { value: null },
            groups: { value: [] }
        }
    });

}, '1.0', {
    requires: [
        'handlebars',
        'event-custom',
        'node',
        'transition',
        'view'
    ]
});

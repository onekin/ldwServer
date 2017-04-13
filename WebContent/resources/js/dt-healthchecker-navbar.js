YUI.add('dt-healthchecker-navbar', function(Y) {

    DT.Navbar = Y.Base.create('navbar', Y.View, [], {
        events: {
            '.navbar-nav a': {
                click: function(e) {

                    var target = null;
                    if (e.target.get('tagName').toLowerCase() != 'a') target = e.target.ancestor('a');
                    else target = e.target;

                    if (target.hasAttribute('data-status')) {
                        e.preventDefault();

                        if (target.hasClass('enabled')) {
                            target.removeClass('enabled');
                            this.filter = null;
                            this.fire('nav:nofilter');
                        }
                        else {
                            if (this.filter) this.filter.removeClass('enabled');
                            target.addClass('enabled');
                            this.filter = target;
                            this.fire('nav:filter', {
                                filter: target.getAttribute('data-status')
                            });
                        }
                    }
                }
            },
            form: {
                keypress: function(e) {
                    var container = this.get('container');
                    if (e.keyCode == 13) {
                        e.preventDefault();
                        this.fire('nav:search', {
                            value: container.one('input').get('value')
                        });
                    }
                },
                submit: function(e) {
                    e.preventDefault();
                }
            }
        },
        initializer: function() {
            this.publish('nav:search');
            this.publish('nav:filter');
            this.publish('nav:nofilter');
        },
        render: function() {
            var container = this.get('container');
            return this;
        }
    });

}, '1.0', {
    requires: [
        'event-custom',
        'event-key',
        'view'
    ]
});

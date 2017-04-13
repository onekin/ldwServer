YUI.add('dt-healthchecker-app', function(Y) {

    DT.App = Y.Base.create('health', Y.App, [], {
        views: { 
            listView: {
                type: DT.Collapse 
            }
        },
        refiner: null,
        search_text: null,
        list: new Y.LazyModelList({ items: DT.data }),
        initializer: function() {
            var app = this;
            this.nav = new DT.Navbar({
                container: '.navbar'
            }).render();
            this.nav.addTarget(this);
            this.on('nav:search', function(e) {
                this.navigate('/search/' + e.value);
            }, this);
            this.on('nav:filter', function(e) {
                this.refiner = e.filter;
                var view = this.get('activeView');
                view.set('list', this.list.filter({asList: true}, function(item) {
                    if (item.name) {
                        return (Y.Array.indexOf(item.errors, e.filter) > -1) && 
                               (app.search_text == null || 
                                item.name
                                    .toLowerCase()
                                    .search(app.search_text.toLowerCase()) != -1);
                    }
                }));
            }, this);
            this.on('nav:nofilter', function(e) {
                this.refiner = null;
                var view = this.get('activeView');
                view.set('list', this.list.filter({asList: true}, function(item) {
                    if (item.name) {
                        return (app.search_text == null || item.name.toLowerCase().search(app.search_text.toLowerCase()) != -1);
                    }
                }));
            }, this);
        },
        index: function(request, resposne, next) {
            this.showView('listView', {
                list: this.list
            });
        },
        search: function(request, response, next) {
            var text = request.params.query;
            this.search_text = request.params.query;
            var app = this;
            var list = this.list.filter({asList: true}, function(item) {
                if (item.name) {
                    return (item.name.toLowerCase().search(text.toLowerCase()) != -1) && (app.refiner == null || Y.Array.indexOf(item.errors, app.refiner) > -1);
                }
                else return false;
            }, this);
            // this.get('activeView').set('list', );
            this.showView('listView', {
                list: list
            });
        }
    }, {
        ATTRS: {
            routes: {
                value: [{
                    path: '/',  //iker                    
                    callbacks: [
                        'index'
                    ]
                },{
                    path: '/healthchecker/',               
                    callbacks: [
                        'index'
                    ]
                },{
                    path: '/search/:query',
                    callbacks: [
                        'search'
                    ]
                }]
            }
        }
    });
}, '1.0', {
    requires: [
        'app',
        'lazy-model-list',
        'dt-collapse',
        'dt-healthchecker-navbar',
        'event-custom'
    ]
});

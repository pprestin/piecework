define([
  'handlebars',
  'chaplin',
  'lib/utils'
], function(Handlebars, Chaplin, utils) {
  'use strict';

  // Application-specific Handlebars helpers
  // -------------------------------------------

  // Get Chaplin-declared named routes. {{#url "like" "105"}}{{/url}}.
  Handlebars.registerHelper('url', function(routeName) {
    var params = [].slice.call(arguments, 1);
    var options = params.pop();
    return Chaplin.helpers.reverse(routeName, params);
  });

  Handlebars.registerHelper('breadcrumb', function(items) {
    var ignore = true;
    var out = '<ul class="breadcrumb">'

    for(var i=0, l=items.length; i<l; i++) {
        var item = items[i];

        if (item.breadcrumb != null && item.breadcrumb != '')
            ignore = false;

        out += '<li><a class="hide" href="#step/' + item.ordinal + '">' + item.breadcrumb + '</a><span class="inactive-text">' + item.breadcrumb + '</span>';
        if (i<l-1)
            out += ' <span class="divider">»</span></li>';
    }

    if (ignore)
        return '';

    return out + '</li></ul>';
  });

});

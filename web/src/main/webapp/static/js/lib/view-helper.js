define([
  'handlebars',
  'chaplin',
  'moment',
  'lib/utils'
], function(Handlebars, Chaplin, moment, utils) {
  'use strict';

  // Application-specific Handlebars helpers
  // -------------------------------------------

  // Get Chaplin-declared named routes. {{#url "like" "105"}}{{/url}}.
  Handlebars.registerHelper('url', function(routeName) {
    var params = [].slice.call(arguments, 1);
    var options = params.pop();
    return Chaplin.helpers.reverse(routeName, params);
  });

  Handlebars.registerHelper('breadcrumb', function(items, options) {
    var ignore = true;
    var out = '<ul class="breadcrumb">'

    if (items.length > 1) {
        for(var i=0, l=items.length; i<l; i++) {
            var item = items[i];

            if (item.breadcrumb != null && item.breadcrumb != '')
                ignore = false;

            if (i > 0)
                out += '<li><a class="hide" href="' + item.breadcrumbLink + '">' + item.breadcrumb + '</a><span class="inactive-text">' + item.breadcrumb + '</span>';
            else
                out += '<li><a href="' + item.breadcrumbLink + '">' + item.breadcrumb + '</a>';

//            if (i<l-1)
//                out += ' <span class="divider">»</span></li>';
        }
    }

    if (ignore)
        return '';

    return out + '</li></ul>';
  });

    Handlebars.registerHelper('date', function(datetime) {
        if (datetime == null)
            return '';
        return moment(datetime).format('MMMM Do YYYY, h:mm:ss a');
    });

    Handlebars.registerHelper('datediff', function(a, b) {
        if (a == null || b == null)
          return '';
        var first = moment(a);
        var second = moment(b);
        var days = first.diff(second, 'days');
        if (days > 1)
            return days + ' days';
        var hours = first.diff(second, 'hours');
        if (hours > 1)
            return hours + ' hours';
        var minutes = first.diff(second, 'minutes');
        if (minutes > 1)
            return minutes + ' minutes';
        var seconds = first.diff(second, 'seconds');
        if (seconds > 1)
            return seconds + ' seconds';
        return '';
    });

});

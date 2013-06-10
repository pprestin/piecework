define(['controllers/search-controller'], function() {
  'use strict';

  return function(match) {
    match('', 'search#search');
    match('search:keyword', 'search#search');
  };
});
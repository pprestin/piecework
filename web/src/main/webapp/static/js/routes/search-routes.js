define(['controllers/search-controller'], function() {
  'use strict';

  return function(match) {
    match('', 'search#search');
    match('process/:process/status/:status/keyword/:keyword', 'search#search');
  };
});
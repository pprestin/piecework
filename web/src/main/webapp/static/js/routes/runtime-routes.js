define(['controllers/runtime-controller'], function() {
  'use strict';

  return function(match) {
    match('', 'runtime#search');
  };
});
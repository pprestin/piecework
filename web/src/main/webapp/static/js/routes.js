define(['controllers/designer-controller'], function() {
  'use strict';

  // The routes for the application. This module returns a function.
  // `match` is match method of the Router
  return function(match) {
    match('', 'designer#index');
    match('designer/start', 'designer#start');
//    match('process-basic-information', 'designer#index');
//    match('process-interactions', 'designer#index');
  };
});

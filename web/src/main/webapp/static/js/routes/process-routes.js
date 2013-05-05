define(['controllers/process-designer-controller'], function() {
  'use strict';

  // The routes for the application. This module returns a function.
  // `match` is match method of the Router
  return function(match) {
    match('', 'process-designer#index');
    match('designer/edit', 'process-designer#edit');
    match('designer/edit/:processDefinitionKey', 'process-designer#edit');
    match('designer/configure/:screenId', 'process-designer#configure');
//    match('process-basic-information', 'designer#index');
//    match('process-interactions', 'designer#index');
  };
});

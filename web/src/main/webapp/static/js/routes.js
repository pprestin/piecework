define(['controllers/form-controller'], function() {
  'use strict';

  return function(match) {
    match(':servlet/:access/form', 'form#search');
    match(':servlet/:access/form/:processDefinitionKey/:requestId', 'form#index');
    match(':servlet/:access/form/:processDefinitionKey/:requestId/step/:ordinal', 'form#step');
  };
});
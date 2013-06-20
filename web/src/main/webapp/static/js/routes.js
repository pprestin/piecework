define(['controllers/form-controller'], function() {
  'use strict';

  return function(match) {
    match(':servlet/app/form/:processDefinitionKey/:requestId', 'form#index');
    match(':servlet/app/form/:processDefinitionKey/:requestId/step/:ordinal', 'form#step');
  };
});
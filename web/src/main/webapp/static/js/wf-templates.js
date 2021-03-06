angular.module('wf.templates', []).run(["$templateCache", function($templateCache) {
  $templateCache.put("templates/activate-modal-dialog.html",
//    "<div class=\"modal-dialog\">\n" +
    "    <div class=\"modal-content\">\n" +
    "        <div class=\"modal-header\">\n" +
    "            <button ng-click=\"cancel()\" type=\"button\" class=\"close\" aria-hidden=\"true\">&times;</button>\n" +
    "            <h4 class=\"modal-title\">Are you sure you want to reactivate this process?</h4>\n" +
    "        </div>\n" +
    "        <div class=\"modal-body\">\n" +
    "            <p>Reactivating a process resumes execution.</p>\n" +
    "            <textarea class=\"form-control input-block-level\" placeholder=\"Enter a reason\" ng-model=\"reason\" id=\"activate-reason\" rows=\"4\"></textarea>\n" +
    "        </div>\n" +
    "        <div class=\"modal-footer\">\n" +
    "            <button ng-click=\"cancel()\" type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\" aria-hidden=\"true\">Cancel</button>\n" +
    "            <button ng-click=\"ok(reason)\" type=\"button\" id=\"activate-button\" class=\"btn btn-primary\">Activate</button>\n" +
    "        </div>\n" +
//    "    </div>\n" +
    "</div>");
  $templateCache.put("templates/assign-modal-dialog.html",
//    "<div class=\"modal-dialog\">\n" +
    "    <div class=\"modal-content\">\n" +
    "        <div class=\"modal-header\">\n" +
    "            <button ng-click=\"cancel()\" type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-hidden=\"true\">&times;</button>\n" +
    "            <h4 class=\"modal-title\">Assign Task</h4>\n" +
    "        </div>\n" +
    "        <div class=\"modal-body\" style=\"min-height:120px\">\n" +
    "            <div data-wf-notifications></div>" +
    "            <div class=\"form-group\">\n" +
    "                <label>Assignee</label>\n" +
    "                <div>\n" +
    "                    <input ng-model=\"assignee\" class=\"form-control\" placeholder=\"Enter name\" style=\"width:300px\" type=\"text\" typeahead=\"person for person in getPeople($viewValue) | filter:$viewValue | limitTo:8\" typeahead-wait-ms=\"300\" />\n" +
    "\n" +
    "                    <p/>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <div class=\"modal-footer\">\n" +
    "            <button ng-click=\"cancel()\" type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\" aria-hidden=\"true\">Cancel</button>\n" +
    "            <button ng-click=\"ok(assignee, assigneeId)\" type=\"button\" class=\"btn btn-primary\" id=\"assign-button\" ng-disabled=\"assignee == null\"><i ng-show=\"assigning\" class='fa fa-spinner fa-spin fa-lg'></i> Assign</button>\n" +
    "        </div>\n" +
//    "    </div>\n" +
    "</div>\n");
  $templateCache.put("templates/cancel-modal-dialog.html",
//    "<div class=\"modal-dialog\">\n" +
    "    <div class=\"modal-content\">\n" +
    "        <div class=\"modal-header bg-danger\">\n" +
    "            <button type=\"button\" class=\"close\" ng-click=\"cancel()\" aria-hidden=\"true\">&times;</button>\n" +
    "            <h4 class=\"modal-title\">Are you sure you want to cancel/delete this process?</h4>\n" +
    "        </div>\n" +
    "        <div class=\"modal-body\">\n" +
    "            <div data-wf-notifications></div>" +
    "            <p>Deleting a process <b>permanently</b> stops execution and <u>cannot</u> be reversed. It also <b>permanently purges</b> any restricted data stored.</p>\n" +
    "            <textarea class=\"form-control input-block-level\" placeholder=\"Enter a reason\" ng-model=\"reason\" id=\"delete-reason\" rows=\"4\"></textarea>\n" +
    "        </div>\n" +
    "        <div class=\"modal-footer\">\n" +
    "            <button ng-click=\"cancel()\" type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\" aria-hidden=\"true\">Cancel</button>\n" +
    "            <button ng-click=\"ok(reason)\" type=\"button\" class=\"btn btn-danger\" id=\"delete-button\">Delete</button>\n" +
    "        </div>\n" +
//    "    </div>\n" +
    "</div>");
  $templateCache.put("templates/columns-modal-dialog.html",
//    "<div class=\"modal-dialog\">\n" +
    "    <div class=\"modal-content\">\n" +
    "        <div class=\"modal-header\">\n" +
    "            <button type=\"button\" class=\"close\" ng-click=\"cancel()\" aria-hidden=\"true\">&times;</button>\n" +
    "            <h4 class=\"modal-title\">Columns</h4>\n" +
    "        </div>\n" +
    "        <div class=\"modal-body\">\n" +
    "           <ul>" +
    "               <li data-ng-repeat=\"facet in application.facets\"><input type=\"checkbox\" data-ng-checked=\"facet.selected\" data-ng-click=\"application.selectFacet(facet)\">&nbsp;&nbsp;&nbsp;{{facet.label}}</li>" +
    "           </ul>" +
    "        </div>\n" +
    "        <div class=\"modal-footer\">\n" +
    "            <button ng-click=\"cancel()\" type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\" aria-hidden=\"true\">Close</button>\n" +
//    "            <button ng-click=\"ok(reason)\" type=\"button\" id=\"activate-button\" class=\"btn btn-primary\">Activate</button>\n" +
    "        </div>\n" +
//    "    </div>\n" +
    "</div>");
  $templateCache.put("templates/comment-modal-dialog.html",
//    "<div class=\"modal-dialog\">\n" +
    "    <div class=\"modal-content\">\n" +
    "        <div class=\"modal-body\">\n" +
    "            <div data-wf-notifications></div>" +
    "            <div class=\"row\">\n" +
    "                <div class=\"col-lg-12 col-sm-12\">\n" +
    "                    <textarea class=\"form-control input-block-level\" placeholder=\"Enter a comment\" ng-model=\"comment\" id=\"attach-comment\" rows=\"4\"></textarea>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <div class=\"modal-footer\">\n" +
    "            <button ng-click=\"cancel()\" type=\"button\" class=\"btn btn-default\" aria-hidden=\"true\">Cancel</button>\n" +
    "            <button ng-click=\"ok(comment)\" type=\"button\" id=\"attach-button\" class=\"btn btn-primary\">Attach</button>\n" +
    "        </div>\n" +
//    "    </div>\n" +
    "</div>");
  $templateCache.put("templates/delete-modal-dialog.html",
//    "<div class=\"modal-dialog\">\n" +
    "    <div class=\"modal-content\">\n" +
    "        <div class=\"modal-header\">\n" +
    "            <h3>{{entityToDelete.title}}</h3>\n" +
    "        </div>\n" +
    "        <div class=\"modal-body\">\n" +
    "            <div data-wf-notifications></div>" +
    "            <span ng-bind-html=\"entityToDelete.text\"></span>\n" +
    "        </div>\n" +
    "        <div class=\"modal-footer\">\n" +
    "            <button class=\"btn btn-danger\" type=\"button\" ng-click=\"ok()\">Yes, delete it</button>\n" +
    "            <button class=\"btn btn-default\" type=\"button\" ng-click=\"cancel()\">Cancel</button>\n" +
    "        </div>\n" +
//    "    </div>\n" +
    "</div>");
  $templateCache.put("templates/history-modal-dialog.html",
//    "<div class=\"modal-dialog\">\n" +
    "    <div class=\"modal-content\">\n" +
    "        <div class=\"modal-header\">\n" +
    "            <button ng-click=\"cancel()\" type=\"button\" class=\"close\" type=\"button\" aria-hidden=\"true\">&times;</button>\n" +
    "            <h4 class=\"modal-title\">History</h4>\n" +
    "        </div>\n" +
    "        <div class=\"modal-body\">\n" +
    "            <div ng-show=\"loading\">\n" +
    "                <p class=\"text-center\"><i class=\"fa fa-spinner fa-spin fa-3x\"></i></p>\n" +
    "            </div>\n" +
    "            <div ng-show=\"!loading\">\n" +
    "                <strong>{{history.processInstanceLabel}}</strong>\n" +
    "                <p class=\"muted\">{{history.processDefinitionLabel}}</p>\n" +
    "\n" +
    "                <table class=\"table table-condensed\">\n" +
    "                    <tr>\n" +
    "                        <td><i class=\"fa fa-play-circle\"></i></td>\n" +
    "                        <td>Started</td>\n" +
    "                        <td></td>\n" +
    "                        <td>{{history.initiator.displayName}}<br/>\n" +
    "                            <span class=\"text-muted\">{{history.startTime|date:'MMM d, y H:mm'}}</span></td>\n" +
    "                    </tr>\n" +
    "\n" +
    "                    <tr ng-repeat=\"event in history.events\">\n" +
    "                        <td ng-if=\"event.type == 'task'\"><i class=\"fa fa-puzzle-piece\"></i></td>\n" +
    "                        <td ng-if=\"event.type == 'task'\">{{event.task.taskLabel}}<br/>\n" +
    "                            <span class=\"text-muted\">{{event.task.taskStatus}}</span>\n" +
    "                        </td>\n" +
    "                        <td ng-if=\"event.type == 'task'\">{{datediff(event.task.endTime, event.task.startTime)}}</td>\n" +
    "\n" +
    "                        <td ng-if=\"event.type == 'operation'\">\n" +
    "                            <span ng-switch on=\"event.operation.type\">\n" +
    "                                <i data-ng-switch-when=\"cancellation\" class=\"fa fa-ban\"></i>\n" +
    "                                <i data-ng-switch-when=\"suspension\" class=\"fa fa-pause\"></i>\n" +
    "                                <i data-ng-switch-when=\"activation\" class=\"fa fa-play\"></i>\n" +
    "                                <i data-ng-switch-when=\"assignment\" class=\"fa fa-user\"></i>\n" +
    "                                <i data-ng-switch-default class=\"fa fa-wrench\"></i>\n" +
    "                            </span>\n" +
    "                        </td>\n" +
    "                        <td ng-if=\"event.type == 'operation'\">{{event.description}}<br/>\n" +
    "                            <span class=\"text-muted\">{{event.operation.reason}}</span>\n" +
    "                        </td>\n" +
    "                        <td ng-if=\"event.type == 'operation'\"></td>\n" +
    "                        <td>\n" +
    "                            {{event.user.displayName}}<br/>\n" +
    "                            <span class=\"text-muted\">{{event.date|date:'MMM d, y H:mm'}}</span>\n" +
    "                        </td>\n" +
    "                    </tr>\n" +
    "\n" +
    "                    <tr ng-if=\"history.endTime\">\n" +
    "                        <td><i class=\"fa fa-power-off\"></i></td>\n" +
    "                        <td>Complete</td>\n" +
    "                        <td></td>\n" +
    "                        <td>{{history.endTime|date:'MMM d, y H:mm'}}</td>\n" +
    "                    </tr>\n" +
    "                </table>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <div class=\"modal-footer\">\n" +
    "            <button ng-click=\"cancel()\" type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\" aria-hidden=\"true\">Close</button>\n" +
    "        </div>\n" +
//    "    </div>\n" +
    "</div>");
  $templateCache.put("templates/restart-modal-dialog.html",
//    "<div class=\"modal-dialog\">\n" +
    "    <div class=\"modal-content\">\n" +
    "        <div class=\"modal-header\">\n" +
    "            <button ng-click=\"cancel()\" type=\"button\" class=\"close\" aria-hidden=\"true\">&times;</button>\n" +
    "            <h4 class=\"modal-title\">Are you sure you want to restart this process?</h4>\n" +
    "        </div>\n" +
    "        <div class=\"modal-body\">\n" +
    "            <p>Restarting a process creates a new instance with the same data as the original and kicks it off</p>\n" +
    "            <textarea class=\"form-control input-block-level\" placeholder=\"Enter a reason\" ng-model=\"reason\" id=\"restart-reason\" rows=\"4\"></textarea>\n" +
    "        </div>\n" +
    "        <div class=\"modal-footer\">\n" +
    "            <button ng-click=\"cancel()\" type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\" aria-hidden=\"true\">Cancel</button>\n" +
    "            <button ng-click=\"ok(reason)\" type=\"button\" id=\"restart-button\" class=\"btn btn-primary\">Restart</button>\n" +
    "        </div>\n" +
//    "    </div>\n" +
    "</div>");
  $templateCache.put("templates/suspend-modal-dialog.html",
//    "<div class=\"modal-dialog\">\n" +
    "    <div class=\"modal-content\">\n" +
    "        <div class=\"modal-header\">\n" +
    "            <button ng-click=\"cancel()\" type=\"button\" class=\"close\" aria-hidden=\"true\">&times;</button>\n" +
    "            <h4 class=\"modal-title\">Are you sure you want to suspend this process?</h4>\n" +
    "        </div>\n" +
    "        <div class=\"modal-body\">\n" +
    "            <div data-wf-notifications></div>" +
    "            <p>Suspending a process pauses execution.</p>\n" +
    "            <textarea class=\"form-control input-block-level\" placeholder=\"Enter a reason\" ng-model=\"reason\" id=\"suspend-reason\" rows=\"4\"></textarea>\n" +
    "        </div>\n" +
    "        <div class=\"modal-footer\">\n" +
    "            <button ng-click=\"cancel()\" type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\" aria-hidden=\"true\">Cancel</button>\n" +
    "            <button ng-click=\"ok(reason)\" type=\"button\" id=\"suspend-button\" class=\"btn btn-primary\">Suspend</button>\n" +
    "        </div>\n" +
//    "    </div>\n" +
    "</div>");
  $templateCache.put('templates/alert-modal-dialog.html',
    '    <div class="modal-content">\n' +
    '        <div class="modal-header">\n' +
    '            <h4 class="modal-title">ALERT</h4>\n' +
    '        </div>\n' +
    '        <div class="modal-body">\n' +
    '            <p>{{message}}</p>\n' +
    '        </div>\n' +
    '        <div class="modal-footer">\n' +
    '            <button ng-click="ok()" type="button" id="ok-button" class="btn btn-primary">OK</button>\n' +
    '        </div>\n' +
    '</div>');
  $templateCache.put("templates/attachments.html",
    "<div data-ng-show=\"state.isViewingAttachments\" class=\"pw-attachments col-md-4\">\n" +
    "        <div class=\"panel panel-default\">\n" +
    "            <div class=\"panel-heading\">\n" +
    "                <button data-ng-click=\"editAttachments()\" class=\"close\" type=\"button\"><i ng-class=\"state.isEditingAttachments ? 'fa fa-unlock' : 'fa fa-lock'\" class=\"fa fa-lock\"></i></button>\n" +
    "                <span class=\"lead\">Notes</span>\n" +
    "            </div>\n" +
    "            <ul class=\"list-group\">\n" +
    "                <li class=\"list-group-item\" data-ng-show=\"!state.attachments\">No attachments</li>\n" +
    "                <li class=\"list-group-item\" data-ng-repeat=\"attachment in state.attachments\">\n" +
    "                    <div class=\"row\">\n" +
    "                        <div class=\"col-md-12\">\n" +
    "                            <button data-ng-show=\"state.isEditingAttachments\" data-ng-click=\"deleteAttachment(attachment)\" class=\"delete-attachments-button text-danger close \" type=\"button\">&times;</button>\n" +
    "                            <span data-ng-if=\"attachment.name == 'comment'\" title=\"{{attachment.description}}\">{{attachment.description}}</span>\n" +
    "                            <span data-ng-if=\"attachment.name != 'comment'\" class=\"pw-attachment-file\">\n" +
    "                                <i class=\"fa fa-download\"></i> <a href=\"{{attachment.link}}\" target=\"_self\" title=\"{{attachment.description}}\">{{attachment.name}}</a>\n" +
    "                            </span>\n" +
    "                        </div>\n" +
    "                    </div>\n" +
    "                    <div class=\"row\">\n" +
    "                        <div class=\"col-md-12\">\n" +
    "                            <div class=\"pull-right text-muted\">{{attachment.user.displayName}}</div>\n" +
    "                        </div>\n" +
    "                    </div>\n" +
    "                    <div class=\"row\">\n" +
    "                        <div class=\"col-md-12\">\n" +
    "                            <div class=\"pull-right text-muted\">{{attachment.lastModified|date:'MMM d, y H:mm'}}</div>\n" +
    "                        </div>\n" +
    "                    </div>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>\n" +
    "    </div>");
  $templateCache.put("templates/breadcrumbs.html",
    "<div class=\"crumbs\" data-ng-if=\"form.steps && form.steps.length > 0\">\n" +
    "            <ul class=\"breadcrumb-stacked\" data-ng-if=\"form.steps && form.steps.length > 0\">\n" +
    "                <li ng-repeat=\"step in form.steps\" ng-class=\"wizard.isActiveStep(form, step) ? 'active' : ''\">\n" +
    "                    <a ng-click=\"wizard.changeStep(form, step.ordinal)\" href=\"\" class=\"list-group-item-text\">{{step.breadcrumb}}</a>\n" +
    "                </li>\n" +
    "            </ul>\n" +
    "        </div>");
  $templateCache.put("templates/container.html",
    "    <wf-notifications></wf-notifications>\n" +
    "    <div class=\"row\"><h2>{{container.title}}</h2></div>\n" +
    "    <wf-status form=\"form\"></wf-status>\n" +
    "    <div ng-class=\"state.isViewingAttachments && 'wf-expanded col-md-8'\">\n" +
    "        <div class=\"row\">\n" +
    "            <div class=\"screen\" id=\"{{container.containerId}}\">\n" +
    "                <wf-fieldset form=\"form\" container=\"container\"></wf-fieldset>\n" +
    "                <div class=\"screen-footer\">\n" +
    "                    <wf-buttonbar form=\"form\" container=\"container\"></wf-buttonbar>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <wf-attachments form=\"form\"></wf-attachments>\n");
   $templateCache.put("templates/field.html",
    "    <label data-ng-if=\"field.label\" data-ng-bind=\"field.label\"></label>\n" +
    "    <div data-ng-class=\"field.cssClass\" data-ng-repeat=\"n in range(1, field.maxInputs)\" data-ng-switch on=\"field.type\" class=\"form-group\">\n" +
    "        <div data-ng-switch-when=\"html\" data-ng-bind-html=\"field.defaultValue\">{{field.defaultValue}}</div>\n" +
    "        <div data-ng-switch-when=\"checkbox\">\n" +
    "            <div class=\"checkbox\" data-ng-repeat=\"option in field.options\">\n" +
    "                <label>\n" +
    "                    <input data-ng-change=\"onFieldChange(field)\" data-ng-disabled=\"!field.editable\" data-ng-model=\"field.value\" data-ng-checked=\"isCheckboxChecked(field, option)\" data-ng-readonly=\"field.readonly\" data-ng-required=\"{{field.required}}\" data-ng-true-value=\"{{option.value}}\" name=\"{{field.name}}\" value=\"{{option.value}}\" type=\"checkbox\"> {{option.label}}\n" +
    "                </label>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <input data-ng-switch-when=\"date\" data-ng-disabled=\"!field.editable\" data-ng-readonly=\"field.readonly\" data-ng-required=\"field.required\" class=\"form-control\" type=\"datetime-local\" name=\"{{field.name}}\" value=\"{{field.value|date:&quot;yyyy-MM-dd'T'HH:mm:ss.00&quot;}}\"/>\n" +
    "        <div data-ng-switch-when=\"radio\">\n" +
    "            <div class=\"radio\" data-ng-repeat=\"option in field.options\">\n" +
    "                <label>\n" +
    "                    <input data-ng-change=\"onFieldChange(field)\" data-ng-disabled=\"!field.editable\" data-ng-checked=\"{{option.value == field.value}}\" data-ng-model=\"field.value\" data-ng-readonly=\"field.readonly\" data-ng-required=\"{{field.required}}\" type=\"radio\" name=\"{{field.name}}\" value=\"{{option.value}}\"> {{option.label}}\n" +
    "                </label>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "        <div data-ng-switch-when=\"select-one\">\n" +
    "            <select data-ng-change=\"onFieldChange(field)\" data-ng-disabled=\"!field.editable\" data-ng-model=\"field.value\" data-ng-readonly=\"field.readonly\" data-ng-required=\"{{field.required}}\" class=\"form-control\" name=\"{{field.name}}\" >\n" +
    "                <option></option>" +
    "                <option data-ng-repeat=\"option in field.options\" value=\"{{option.value}}\" data-ng-selected=\"{{option.value == field.value}}\">\n" +
    "                    {{option.label!='' && option.label || option.value}}\n" +
    "                </option>\n" +
    "            </select>\n" +
    "        </div>\n" +
    "        <div data-ng-switch-when=\"file\">\n" +
    "            <a data-ng-repeat=\"value in field.values\" href=\"{{value.link ? value.link : value}}\" rel=\"external\" target=\"_self\">{{value.name}}</a>\n" +
    "            <!--<img class=\"thumbnail\" data-ng-src=\"{{field.value.link ? field.value.link : field.value}}\" data-ng-show=\"field.value\"/>-->\n" +
    "            <input data-ng-change=\"onFieldChange(field)\" data-ng-disabled=\"!field.editable\" data-ng-model=\"file\" data-ng-readonly=\"field.readonly\" class=\"form-control\" name=\"{{field.name}}\" type=\"file\"/>\n" +
    "        </div>\n" +
    "        <div data-ng-switch-when=\"iframe\">\n" +
    "            <iframe data-ng-repeat=\"value in field.values\" src=\"{{getInlineUrl(value)}}\"></iframe>\n" +
    "        </div>\n" +
    "        <div data-ng-switch-when=\"person\">\n" +
    "            <input data-ng-model=\"field.value.displayName\" data-ng-if=\"field.readonly\" class=\"form-control\" type=\"text\" disabled/>\n" +
    "            <input data-ng-change=\"onFieldChange(field)\" data-ng-disabled=\"!field.editable\" data-ng-model=\"field.value\" data-ng-maxlength=\"{{field.maxValueLength}}\" data-ng-minlength=\"{{field.minValueLength}}\" data-ng-pattern=\"{{field.pattern}}\" ng-if=\"!field.readonly\" ng-required=\"field.required\" class=\"form-control\" placeholder=\"{{field.placeholder ? field.placeholder : 'Enter name'}}\" type=\"text\" typeahead=\"person for person in getPeople($viewValue) | filter:$viewValue | limitTo:8\" typeahead-wait-ms=\"300\" />\n" +
    "            <input data-ng-if=\"field.editable\" value=\"{{field.value.userId ? field.value.userId : field.value}}\" name=\"{{field.name}}\" type=\"hidden\"/>\n" +
    "        </div>\n" +
    "        <textarea data-ng-change=\"onFieldChange(field)\" data-ng-switch-when=\"textarea\" data-ng-disabled=\"!field.editable\" data-ng-model=\"field.value\" data-ng-maxlength=\"{{field.maxValueLength}}\" data-ng-minlength=\"{{field.minValueLength}}\" data-ng-pattern=\"/{{field.pattern}}/\" data-ng-readonly=\"{{field.readonly}}\" data-ng-required=\"field.required\" class=\"form-control\" name=\"{{field.name}}\" placeholder=\"{{field.placeholder}}\"></textarea>\n" +
    "        <div data-ng-switch-default>\n" +
    "            <input data-ng-if=\"field.editable\" data-ng-change=\"onFieldChange(field)\" data-ng-disabled=\"!field.editable\" data-ng-model=\"field.values[n-1]\" data-ng-minlength=\"{{field.minValueLength}}\" data-wf-inputmask=\"{{field.mask}}\" data-ng-pattern=\"/{{field.pattern}}/\" data-ng-readonly=\"field.readonly\" class=\"form-control\" type=\"{{field.type}}\" name=\"{{field.name}}\" placeholder=\"{{field.placeholder}}\" size=\"{{field.displayValueLength}}\"/>\n" +
    "            <input data-ng-if=\"!field.editable\" data-ng-disabled=\"!field.editable\" data-ng-model=\"field.values[n-1]\" data-ng-readonly=\"field.readonly\" class=\"form-control\" type=\"{{field.type}}\" name=\"{{field.name}}\" placeholder=\"{{field.placeholder}}\" size=\"{{field.displayValueLength}}\"/>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <span data-ng-repeat=\"message in field.messages\" class=\"help-block text-danger\">{{message.text}}</span>\n");

//  $templateCache.put("templates/field.html",
//    "    <label data-ng-if=\"field.label\">{{field.label}}</label>\n" +
//    "    <div data-ng-class=\"field.cssClass\" data-ng-repeat=\"n in range(1, field.maxInputs)\" data-ng-switch on=\"field.type\" class=\"form-group\">\n" +
//    "        <div data-ng-switch-when=\"html\" data-ng-bind-html=\"field.defaultValue\">{{field.defaultValue}}</div>\n" +
//    "        <div data-ng-switch-when=\"checkbox\">\n" +
//    "            <div class=\"checkbox\" data-ng-repeat=\"option in field.options\">\n" +
//    "                <label>\n" +
//    "                    <input data-ng-change=\"onFieldChange(field)\" data-ng-disabled=\"!field.editable\" data-ng-model=\"field.value\" data-ng-checked=\"isCheckboxChecked(field, option)\" data-ng-readonly=\"field.readonly\" data-ng-required=\"{{field.required}}\" data-ng-true-value=\"{{option.value}}\" name=\"{{field.name}}\" value=\"{{option.value}}\" type=\"checkbox\"> {{option.label}}\n" +
//    "                </label>\n" +
//    "            </div>\n" +
//    "        </div>\n" +
//    "        <input data-ng-switch-when=\"date\" data-ng-disabled=\"!field.editable\" data-ng-readonly=\"field.readonly\" data-ng-required=\"field.required\" class=\"form-control\" type=\"datetime-local\" name=\"{{field.name}}\" value=\"{{field.value|date:&quot;yyyy-MM-dd'T'HH:mm:ss.00&quot;}}\"/>\n" +
//    "        <div data-ng-switch-when=\"radio\">\n" +
//    "            <div class=\"radio\" data-ng-repeat=\"option in field.options\">\n" +
//    "                <label>\n" +
//    "                    <input data-ng-change=\"onFieldChange(field)\" data-ng-disabled=\"!field.editable\" data-ng-checked=\"{{option.value == field.value}}\" data-ng-model=\"field.value\" data-ng-readonly=\"field.readonly\" data-ng-required=\"{{field.required}}\" type=\"radio\" name=\"{{field.name}}\" value=\"{{option.value}}\"> {{option.label}}\n" +
//    "                </label>\n" +
//    "            </div>\n" +
//    "        </div>\n" +
//    "        <div data-ng-switch-when=\"select-one\">\n" +
//    "            <select data-ng-change=\"onFieldChange(field)\" data-ng-disabled=\"!field.editable\" data-ng-model=\"field.value\" data-ng-readonly=\"field.readonly\" data-ng-required=\"{{field.required}}\" class=\"form-control\" name=\"{{field.name}}\" >\n" +
//    "                <option></option>" +
//    "                <option data-ng-repeat=\"option in field.options\" value=\"{{option.value}}\" data-ng-selected=\"{{option.value == field.value}}\">\n" +
//    "                    {{option.label!='' && option.label || option.value}}\n" +
//    "                </option>\n" +
//    "            </select>\n" +
//    "        </div>\n" +
//    "        <div data-ng-switch-when=\"file\">\n" +
//    "            <a data-ng-repeat=\"value in field.values\" href=\"{{value.link ? value.link : value}}\" rel=\"external\" target=\"_self\">{{value.name}}</a>\n" +
//    "            <!--<img class=\"thumbnail\" data-ng-src=\"{{field.value.link ? field.value.link : field.value}}\" data-ng-show=\"field.value\"/>-->\n" +
//    "            <input data-ng-change=\"onFieldChange(field)\" data-ng-disabled=\"!field.editable\" data-ng-model=\"file\" data-ng-readonly=\"field.readonly\" class=\"form-control\" name=\"{{field.name}}\" type=\"file\"/>\n" +
//    "        </div>\n" +
//    "        <div data-ng-switch-when=\"iframe\">\n" +
//    "            <iframe data-ng-repeat=\"value in field.values\" src=\"{{getInlineUrl(value)}}\"></iframe>\n" +
//    "        </div>\n" +
//    "        <div data-ng-switch-when=\"person\">\n" +
//    "            <input data-ng-model=\"field.value.displayName\" data-ng-if=\"field.readonly\" class=\"form-control\" type=\"text\" disabled/>\n" +
//    "            <input data-ng-change=\"onFieldChange(field)\" data-ng-disabled=\"!field.editable\" data-ng-model=\"field.value\" data-ng-maxlength=\"{{field.maxValueLength}}\" data-ng-minlength=\"{{field.minValueLength}}\" data-ng-pattern=\"{{field.pattern}}\" ng-if=\"!field.readonly\" ng-required=\"field.required\" class=\"form-control\" placeholder=\"{{field.placeholder ? field.placeholder : 'Enter name'}}\" type=\"text\" typeahead=\"person for person in getPeople($viewValue) | filter:$viewValue | limitTo:8\" typeahead-wait-ms=\"300\" />\n" +
//    "            <input data-ng-if=\"field.editable\" value=\"{{field.value.userId ? field.value.userId : field.value}}\" name=\"{{field.name}}\" type=\"hidden\"/>\n" +
//    "        </div>\n" +
//    "        <textarea data-ng-change=\"onFieldChange(field)\" data-ng-switch-when=\"textarea\" data-ng-disabled=\"!field.editable\" data-ng-model=\"field.value\" data-ng-maxlength=\"{{field.maxValueLength}}\" data-ng-minlength=\"{{field.minValueLength}}\" data-ng-pattern=\"/{{field.pattern}}/\" data-ng-readonly=\"{{field.readonly}}\" data-ng-required=\"field.required\" class=\"form-control\" name=\"{{field.name}}\" placeholder=\"{{field.placeholder}}\"></textarea>\n" +
//    "        <div data-ng-switch-default>\n" +
//    "            <input data-ng-if=\"field.editable\" data-ng-change=\"onFieldChange(field)\" data-ng-disabled=\"!field.editable\" data-ng-model=\"field.values[n-1]\" data-ng-minlength=\"{{field.minValueLength}}\" data-wf-inputmask=\"{{field.mask}}\" data-ng-pattern=\"/{{field.pattern}}/\" data-ng-readonly=\"field.readonly\" class=\"form-control\" type=\"{{field.type}}\" name=\"{{field.name}}\" placeholder=\"{{field.placeholder}}\" size=\"{{field.displayValueLength}}\"/>\n" +
//    "            <input data-ng-if=\"!field.editable\" data-ng-disabled=\"!field.editable\" data-ng-model=\"field.values[n-1]\" data-ng-readonly=\"field.readonly\" class=\"form-control\" type=\"{{field.type}}\" name=\"{{field.name}}\" placeholder=\"{{field.placeholder}}\" size=\"{{field.displayValueLength}}\"/>\n" +
//    "        </div>\n" +
//    "    </div>\n" +
//    "    <span data-ng-repeat=\"message in field.messages\" class=\"help-block text-danger\">{{message.text}}</span>\n");
  $templateCache.put("templates/fieldset.html",
    "<ul data-ng-model=\"container.fields\">\n" +
    "        <li data-ng-if=\"isVisible(field)\" data-ng-class=\"field.cssClass\" data-ng-repeat=\"field in container.fields\" class=\"pw-field\">\n" +
    "            <div data-wf-field field=\"field\"></div>\n" +
    "        </li>\n" +
    "    </ul>");
  $templateCache.put("templates/notifications.html",
    "<div data-ng-if=\"notifications && notifications.length > 0\" data-ng-show=\"notifications\" class=\"alert alert-danger\">\n" +
    "        <button type=\"button\" class=\"close\" type=\"button\" data-ng-click=\"notifications.length=0\" aria-hidden=\"true\">&times;</button>\n" +
    "        <h4 data-ng-if=\"notifications[0].title\">{{notifications[0].title}}</h4>\n" +
    "        <ul>\n" +
    "            <li data-ng-repeat=\"notification in notifications\" data-ng-bind-html=\"notification.message\"></li>\n" +
    "        </ul>\n" +
    "    </div>");
  $templateCache.put("templates/review.html",
    "    <wf-notifications></wf-notifications>\n" +
    "    <wf-status form=\"form\"></wf-status>\n" +
    "    <div>\n" +
//    "        <form class=\"form form-default\" action=\"{{form.action}}\" method=\"POST\" enctype=\"multipart/form-data\" novalidate>\n" +
    "            <div class=\"row\">\n" +
    "                <div class=\"crumbs col-md-3\" data-ng-if=\"form.steps && form.steps.length > 0\">\n" +
    "                    <ul class=\"breadcrumb-stacked\">\n" +
    "                        <li ng-repeat=\"step in form.steps\" ng-class=\"wizard.isActiveStep(form, step) ? 'active' : ''\">\n" +
    "                            <a data-ng-class=\"step.breadcrumbCssClass\" data-ng-show=\"wizard.isAvailableStep(form, step)\" ng-click=\"form.activeStepOrdinal = step.ordinal\" href=\"\" class=\"list-group-item-text\">{{step.breadcrumb}} <i class=\"fa fa-warning invalid-only\" title=\"This section has validation errors\"></i></a>\n" +
    "                            <span ng-show=\"!wizard.isAvailableStep(form, step)\">{{step.breadcrumb}}</span>\n" +
    "                        </li>\n" +
    "                    </ul>\n" +
    "                </div>\n" +
    "                <div ng-class=\"state.isViewingAttachments ? ((form.steps && form.steps.length > 0) ? 'col-md-5' : 'col-md-8') : 'col-md-9'\" class=\"screen\">\n" +
    "                    <h2>{{form.container.title}}</h2>\n" +
    "                    <div ng-repeat=\"step in form.steps\">\n" +
    "                        <wf-step form=\"form\" step=\"step\" active=\"wizard.isActiveStep(form, step)\" current=\"false\"></wf-step>\n" +
    "                    </div>\n" +
    "                    <div class=\"screen-footer\">\n" +
    "                        <wf-buttonbar form=\"form\" container=\"form.container\"></wf-buttonbar>\n" +
    "                        <div class=\"clearfix\"></div>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "                <wf-attachments form=\"form\"></wf-attachments>\n" +
    "            </div>\n" +
//    "        </form>\n" +
    "    </div>");
//  $templateCache.put("templates/status.html",
//    '   <div class="container"><div class="row" data-ng-show="form.container.readonly" data-ng-switch="form.state">\n' +
//    '        <div data-ng-switch-when="assigned" class="alert alert-info">\n' +
//    '            <strong>This form is assigned to {{form.task.assignee ? form.task.assignee.displayName : \'Nobody\'}}</strong> - to take action, you will need to assign it to yourself.\n' +
//    '            <button data-ng-click="claim()" class="btn btn-default pull-right" type="button">Assign to me</button>\n' +
//    '            <div class="clearfix"></div>\n' +
//    '        </div>\n' +
//    '        <div data-ng-switch-when="unassigned" class="alert alert-info">\n' +
//    '            <strong>This form is not currently assigned</strong> - to modify it, you will need to assign it to yourself.\n' +
//    '            <button data-ng-click="claim()" class="btn btn-default pull-right" type="button">Assign to me</button>\n' +
//    '            <div class="clearfix"></div>\n' +
//    '        </div>\n' +
//    '        <div data-ng-switch-when="completed" class="alert alert-info"><strong>This form can no longer be modified</strong> - it was completed by {{form.task.assignee.displayName}} on {{form.task.endTime"date:\'MMM d, y H:mm\'}}</div>\n' +
//    '        <div data-ng-switch-when="suspended" class="alert alert-info"><strong>This form can no longer be modified</strong> - it has been suspended</div>\n' +
//    '        <div data-ng-switch-when="cancelled" class="alert alert-info"><strong>This form can no longer be modified</strong> - it has been cancelled</div>\n' +
//    '    </div>\n' +
//    '    <div data-ng-if="form.applicationStatusExplanation != null && form.applicationStatusExplanation != \'\'" class="row">\n' +
//    '        <div class="alert alert-danger">\n' +
//    '        <button type="button" class="close" data-ng-click="form.applicationStatusExplanation = null" aria-hidden="true">&times;</button>\n' +
//    '           {{form.applicationStatusExplanation}}\n' +
//    '    </div>\n' +
//    '    </div><div class="row"><div data-ng-if="form.explanation != null && form.explanation.message != null && form.explanation.message != \'\'" class="alert alert-danger">\n' +
//    '        <h4 data-ng-if="form.explanation.message">{{form.explanation.message}}</h4>\n' +
//    '        <p>{{form.explanation.messageDetail}}</p>\n' +
//    '    </div></div></div>');
  $templateCache.put("templates/buttonbar.html",
    "<div data-ng-if=\"!container.readonly && container.buttons.length>0\" class=\"btn-toolbar pull-right\" role=\"toolbar\">\n" +
    "    <div data-ng-repeat=\"button in container.buttons\" class=\"btn-group dropup\">\n" +
    "        <button data-ng-class=\"button.primary && 'btn-primary'\" data-ng-click=\"wizard.clickButton(form, container, button)\" class=\"btn btn-default\" name=\"{{button.name}}\" type=\"{{button.type}}\" value=\"{{button.value}}\">{{button.label}}</button>\n" +
    "        <button data-ng-if=\"button.children.length>0\" type=\"button\" class=\"btn btn-default dropdown-toggle\" data-toggle=\"dropdown\">\n" +
    "            <span class=\"caret\"></span>\n" +
    "        </button>\n" +
    "        <ul data-ng-if=\"button.children.length>0\" class=\"dropdown-menu\">\n" +
    "            <li data-ng-repeat=\"child in button.children\" data-ng-if=\" child.ordinal < container.activeChildIndex \"> \n" +
    "        <button data-ng-click=\"wizard.clickButton(form, container, button)\" class=\"btn-link\" name=\"{{button.name}}\" type=\"{{button.type}}\" value=\"{{child.value}}\">{{child.label}}</button>\n" +
    "            </li>\n" +
    "        </ul>\n" +
    "    </div>\n" +
    "</div>"
    );
  $templateCache.put("templates/form.html",
    "<div data-wf-toolbar></div>\n" +
    "<form data-wf-form=\"\" name=\"myForm\" action=\"/\" method=\"POST\" novalidate=\"novalidate\">\n" +
    "    <div data-wf-page></div>\n" +
    "</form>\n" +
    "\n" +
    "<div class=\"main-footer navbar navbar-fixed-bottom\">\n" +
    "    <div class=\"navbar-inner\">\n" +
    "        <ul>\n" +
    "            <li class=\"nav\"></li>\n" +
    "            <li class=\"nav pull-right\"><h1 style=\"font-weight: normal\">Workflow&nbsp;&nbsp;</h1></li>\n" +
    "        </ul>\n" +
    "    </div>\n" +
    "</div>");
  $templateCache.put("templates/form-list.html",
    "<div class=\"navbar main-navbar\">\n" +
    "    <div class=\"user-information\">\n" +
    "        <div id=\"user-information-name\" class=\"user-information-name pull-right\">\n" +
    "            {{context.user.displayName}}\n" +
    "        </div>\n" +
    "    </div>\n" +
    "\n" +
    "    <div data-wf-searchbar></div>\n" +
    "</div>\n" +
    "<div class=\"container\">\n" +
    "    <div class=\"main-content row\">\n" +
    "        <div data-ng-show=\"notification\" class=\"alert alert-danger\">\n" +
    "            <strong>{{notification.title}}</strong> {{notification.message}}\n" +
    "        </div>\n" +
    "        <div data-wf-searchresults></div>\n" +
    "    </div>\n" +
    "</div>\n" +
    "\n" +
    "<div class=\"main-footer navbar navbar-fixed-bottom\">\n" +
    "    <div class=\"navbar-inner\">\n" +
    "        <ul>\n" +
    "            <li class=\"nav\"></li>\n" +
    "            <li class=\"nav pull-right\"><h1 style=\"font-weight: normal;margin-right:20px\">Workflow</h1></li>\n" +
    "        </ul>\n" +
    "    </div>\n" +
    "</div>");
  $templateCache.put("templates/form-namebar.html",
    "     <nav class=\"navbar navbar-default navbar-inverse pw-navbar-collapse\" style=\"min-height: 20px;margin-bottom: 0px\">\n" +
    "         <div class=\"user-information\">\n" +
    "             <div id=\"user-information-name\" class=\"user-information-name pull-right\">\n" +
    "                 {{form.currentUser.displayName}}\n" +
    "             </div>\n" +
    "         </div>\n" +
    "     </nav>");
  $templateCache.put("templates/form-login.html",
      "             <div id=\"user-information-name\" class=\"user-information-name pull-right\">\n" +
      "                 {{form.currentUser.displayName}}\n" +
      "             </div>\n");
//  $templateCache.put("templates/form-navbar.html",
//    '     <nav class="navbar navbar-default navbar-ex1-collapse" style="border-radius: 0px">\n' +
//    '         <div class="navbar-header">\n' +
//    '             <button data-ng-click="toggleCollapse()" type="button" class="navbar-toggle">\n' +
//    '                 <span class="sr-only">Toggle</span>\n' +
//    '                 <span class="icon-bar"></span>\n' +
//    '                 <span class="icon-bar"></span>\n' +
//    '                 <span class="icon-bar"></span>\n' +
//    '             </button>\n' +
//    '         </div>\n' +
//    '         <div data-ng-class="state.isCollapsed ? \'\' : \'collapse\'" class="navbar-collapse navbar-ex1-collapse">\n' +
//    '             <div class="container"><div class="row">\n' +
//    '                 <div class="navbar-left btn-toolbar">\n' +
//    '                     <button data-ng-click="dialogs.openHistoryModal([form])" data-ng-show="form.history" class="btn btn-default selected-result-btn navbar-btn" id="history-dialog-button" title="History" type="button"><i class="fa fa-calendar-o fa-white"></i></button>\n' +
//    '                     <button data-ng-click="dialogs.openSuspendModal([form])" data-ng-show="form.history && form.task.active" class="btn btn-default navbar-btn" title="Suspend process" type="button"><i class="fa fa-pause fa-white"></i></button>\n' +
//    '                     <button data-ng-click="dialogs.openCancelModal([form])" data-ng-show="form.history && form.task.active" class="btn btn-danger navbar-btn" id="delete-dialog-button" title="Delete process" type="button"><i class="fa fa-trash-o"></i></button>\n' +
//    '                     <button data-ng-click="dialogs.openActivateModal([form])" data-ng-show="form.history && form.task.taskStatus == \'Suspended\'" class="btn btn-default navbar-btn" id="activate-dialog-button" title="Activate process" type="button"><i class="fa fa-play"></i></button>\n' +
//    '                     <button data-ng-click="dialogs.openRestartModal([form])" data-ng-show="form.history && (form.task.taskStatus == \'Cancelled\' "" form.task.taskStatus == \'Completed\')" class="btn btn-default navbar-btn" title="Restart process" type="button"><i class="fa fa-rotate-left"></i></button>\n' +
//    '                 </div>\n' +
//    '                 <div class="navbar-right btn-toolbar">\n' +
//    //'                     <ul class="responsive-pull-right navbar-nav btn-toolbar">  \n' +
//    '                       <div data-wf-assignment-button data-form="form" class="navbar-nav"></div>' +
////    '                         <p data-ng-show="form.history && form.task.active && form.task.assignee" class="navbar-text text-primary">Assigned to {{form.task.assignee.displayName}}</p>\n' +
////    '                         <div data-ng-show="form.task.active" class="btn-group">\n' +
////    '                             <button data-ng-click="dialogs.openAssignModal([form])" class="btn btn-default navbar-btn" id="assign-dialog-button" data-target="#assign-dialog" data-backdrop="false" data-toggle="modal" title="Assign task" type="button"><i class="fa fa-user"></i></button>\n' +
////    '                             <button type="button" class="btn btn-default navbar-btn dropdown-toggle" data-toggle="dropdown">\n' +
////    '                                 <span class="caret"></span>\n' +
////    '                             </button>\n' +
////    '                             <ul class="dropdown-menu">\n' +
////    '                                 <li><a data-ng-click="assignTo('')">Unassign</a></li>\n' +
////    '                                 <li data-ng-show="form.task.candidateAssignees" role="presentation" class="divider"></li>\n' +
////    '                                 <li data-ng-repeat="candidateAssignee in form.task.candidateAssignees"><a data-ng-click="assignTo(candidateAssignee.userId)" class="candidate-assignee" id="{{candidateAssignee.userId}}">Assign to {{candidateAssignee.userId == context.user.userId ? 'me' : candidateAssignee.displayName}}</a></li>\n' +
////    '                             </ul>\n' +
////    '                         </div>\n' +
//    '                         <div class="btn-group"><a data-ng-click="dialogs.openCommentModal([form])" data-ng-show="form.allowAttachments && form.history" class="btn btn-default navbar-btn" id="comment-button" data-target="#comment-dialog" data-backdrop="false" data-toggle="modal" title="Add comment" type="button"><i class="fa fa-comment-o"></i></a></div>\n' +
//    '                         <div class="btn-group">\n' +
//    '                             <form data-ng-show="form.allowAttachments && form.history" class="navbar-left form-inline" action="{{getAttachmentUrl()}}" method="POST" enctype="multipart/form-data" data-file-upload="fileUploadOptions">\n' +
//    '                                 <span class="btn btn-default navbar-btn fileinput-button" data-ng-class="{disabled: disabled}">\n' +
//    '                                       <i ng-hide="state.sending" class="fa fa-paperclip"></i>  <i ng-show="state.sending" class=\'fa fa-paperclip fa-spin\'></i>\n' +
//    '                                       <input type="file" name="attachment" multiple="" ng-disabled="disabled">\n' +
//    '                                 </span>\n' +
//    '                             </form>\n' +
//    '                         </div>\n' +
//    '                         <div class="btn-group"><button data-ng-click="viewAttachments()" data-ng-show="form.allowAttachments && form.history" class="btn btn-default navbar-btn" id="attachments-button" title="View comments and attachments" type="button"><i ng-class="state.isViewingAttachments ? \'fa-folder-open\' : \'fa-folder\'" class="fa fa-folder"></i>&nbsp;<span id="attachment-count">{{form.attachmentCount}}</span></button></div>\n' +
//    '                         <div class="btn-group"><a class="btn btn-default navbar-btn" href="{{form.root}}" rel="external" id="back-button" target="_self" title="Return to task list"><i class="fa fa-arrow-left"></i></a></div>\n' +
//    //'                     </ul>\n' +
//    '                 </div>\n' +
//    '             </div></div>\n' +
//    '         </div>\n' +
//    '     </nav>');
//  $templateCache.put("templates/file.html",
//    );
  $templateCache.put("templates/fileupload.html",
    "   <form data-ng-show=\"form.allowAttachments && form.history\" class=\"navbar-left form-inline\" id=\"fileupload\" action=\"{{getAttachmentUrl()}}\" method=\"POST\" enctype=\"multipart/form-data\" data-file-upload=\"fileUploadOptions\">\n" +
    "       <span class=\"btn btn-default navbar-btn fileinput-button\" data-ng-class=\"{disabled: disabled}\">\n" +
    "           <i ng-hide=\"state.sending\" class=\"fa fa-paperclip\"></i>  <i ng-show=\"state.sending\" class='fa fa-paperclip fa-spin'></i>\n" +
    "           <input type=\"file\" name=\"attachment\" multiple=\"\" ng-disabled=\"disabled\">\n" +
    "       </span>\n" +
    "   </form>\n");
  $templateCache.put("templates/multipage.html",
    "<wf-notifications></wf-notifications>\n" +
    "    <wf-status form=\"form\"></wf-status>\n" +
    "    <div>\n" +
//    "        <form class=\"form form-default\" action=\"{{form.action}}\" method=\"POST\" enctype=\"multipart/form-data\" novalidate>\n" +
    "            <div class=\"row\">\n" +
    "                <div class=\"crumbs col-md-3\" data-ng-if=\"form.steps && form.steps.length > 0\">\n" +
    "                    <ul class=\"breadcrumb-stacked\">\n" +
    "                        <li ng-repeat=\"step in form.steps\" ng-class=\"wizard.isActiveStep(form, step) ? 'active' : ''\">\n" +
    "                            <a ng-click=\"wizard.changeStep(form, step.ordinal)\" href=\"\" class=\"list-group-item-text\">{{step.breadcrumb}}</a>\n" +
    "                        </li>\n" +
    "                    </ul>\n" +
    "                </div>\n" +
    "                <div ng-class=\"state.isViewingAttachments ? 'col-md-5' : 'col-md-9'\" class=\"screen\" id=\"container-{{container.containerId}}\">\n" +
    "                    <h2>{{form.container.title}}</h2>\n" +
    "                    <div ng-repeat=\"step in form.steps\">\n" +
    "                        <wf-step form=\"form\" step=\"step\" active=\"wizard.isActiveStep(form, step)\" current=\"wizard.isCurrentStep(form, step)\"></wf-step>\n" +
    "                    </div>\n" +
    "                    <div class=\"screen-footer\">\n" +
    "                        <wf-buttonbar form=\"form\" container=\"form.container\"></wf-buttonbar>\n" +
    "                        <div class=\"clearfix\"></div>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "                <wf-attachments form=\"form\"></wf-attachments>\n" +
    "            </div>\n" +
//    "        </form>\n" +
    "    </div>");
  $templateCache.put("templates/multistep.html",
    "<wf-notifications></wf-notifications>\n" +
    "    <wf-status form=\"form\"></wf-status>\n" +
//    "    <form class=\"form form-default\" action=\"{{form.action}}\" method=\"POST\" enctype=\"multipart/form-data\">\n" +
    "        <div class=\"row\">\n" +
    "            <div ng-class=\"state.isViewingAttachments ? 'col-md-8' : 'col-md-12'\" class=\"screen\">\n" +
    "                <h2>{{form.container.title}}</h2>\n" +
    "                <div class=\"crumbs\">\n" +
    "                    <ul class=\"breadcrumb\">\n" +
    "                        <li ng-repeat=\"step in form.steps\" ng-class=\"wizard.isActiveStep(form, step) ? 'active' : ''\">\n" +
    "                            <a ng-click=\"wizard.changeStep(form, step.ordinal)\" href=\"\" class=\"list-group-item-text\">{{step.breadcrumb}}</a>\n" +
    "                        </li>\n" +
    "                    </ul>\n" +
    "                </div>\n" +
    "                <div ng-repeat=\"step in form.steps\">\n" +
    "                    <wf-step form=\"form\" step=\"step\" active=\"wizard.isActiveStep(form, step)\" current=\"wizard.isCurrentStep(form, step)\"></wf-step>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "            <wf-attachments form=\"form\"></wf-attachments>\n" +
    "        </div>\n"
//    "    </form>"
    );
  $templateCache.put("templates/page.html",
    "<div class=\"main-content container\" data-ng-switch on=\"form.layout\">\n" +
    "        <div class=\"row\" data-ng-switch-when=\"multipage\">\n" +
    "            <wf-multipage form=\"form\"></wf-multipage>\n" +
    "        </div>\n" +
    "        <div class=\"row\" data-ng-switch-when=\"multistep\">\n" +
    "            <wf-multistep form=\"form\"></wf-multistep>\n" +
    "        </div>\n" +
    "        <div class=\"row\" data-ng-switch-when=\"review\">\n" +
    "            <wf-review form=\"form\"></wf-review>\n" +
    "        </div>\n" +
    "        <div class=\"row\" data-ng-switch-when=\"normal\">\n" +
    "            <wf-container form=\"form\" container=\"form.container\"/>\n" +
    "        </div>\n" +
    "    </div>");
  $templateCache.put("templates/person.html",
    '<div>\n' +
    '   <input data-ng-change="onChange()" data-ng-model="person" data-ng-required="{{required}}" class="form-control" type="text" data-typeahead="person for person in getPeople($viewValue) | filter:$viewValue | limitTo:8" data-typeahead-wait-ms="300" />\n' +
    '   <input value="{{person.userId ? person.userId : person}}" name="{{name}}" type="hidden"/>\n' +
    '</div>');
  $templateCache.put("templates/step.html",
    "<div ng-show=\"active\" data-ng-class=\"step.leaf ? 'panel panel-default step' : 'step'\">\n" +
    "        <div ng-class=\"step.leaf ? 'panel-heading' : ''\"><h4>{{step.title}}</h4></div>\n" +
    "        <div ng-class=\"step.leaf ? 'panel-body' : ''\">\n" +
    "            <wf-fieldset form=\"form\" container=\"step\"></wf-fieldset>\n" +
    "            <div data-ng-repeat=\"child in step.children\">\n" +
    "                <div ng-show=\"active\" data-ng-class=\"child.leaf ? 'panel panel-default child' : 'child'\">\n" +
    "                    <div ng-class=\"child.leaf ? 'panel-heading' : ''\"><h4>{{child.title}}</h4></div>\n" +
    "                    <div ng-class=\"child.leaf ? 'panel-body' : ''\">\n" +
    "                        <wf-fieldset form=\"form\" container=\"child\"></wf-fieldset>\n" +
    "                    </div>\n" +
    "                </div>\n" +
    "            </div>\n" +
    "        </div>\n" +
    "    </div>\n" +
    "    <div data-ng-show=\"current\" class=\"screen-footer\">\n" +
    "        <wf-buttonbar form=\"form\" container=\"step\"></wf-buttonbar>\n" +
    "        <div class=\"clearfix\"></div>\n" +
    "    </div>");
//  $templateCache.put("templates/searchbar.html",
//    "<nav class=\"navbar navbar-default navbar-ex1-collapse\" style=\"margin-bottom: 0px;border-radius: 0px\">\n" +
//    "        <div class=\"navbar-header\">\n" +
//    "            <button data-ng-click=\"state.toggleCollapse()\" type=\"button\" class=\"navbar-toggle\">\n" +
//    "                <span class=\"sr-only\">Toggle</span>\n" +
//    "                <span class=\"icon-bar\"></span>\n" +
//    "                <span class=\"icon-bar\"></span>\n" +
//    "                <span class=\"icon-bar\"></span>\n" +
//    "            </button>\n" +
//    "        </div>\n" +
//    "        <div data-ng-class=\"state.isCollapsed ? '' : 'collapse'\" class=\"navbar-collapse navbar-ex1-collapse\">\n" +
//    "            <div class=\"container\">\n" +
//    "                <form class=\"navbar-form navbar-left form-inline\" role=\"search\">\n" +
//    "                    <div class=\"row\">\n" +
//    "                        <input style=\"width: 400px\" title=\"Search by keyword\" role=\"\" class=\"form-control searchField\" data-ng-model=\"criteria.keyword\" placeholder=\"Search\" id=\"keyword\" type=\"text\">\n" +
//    "                        <button data-ng-click=\"refreshSearch()\" class=\"btn btn-default navbar-btn\" role=\"button\" id=\"instanceSearchButton\" type=\"submit\">&nbsp;&nbsp;<i ng-show=\"searching\" class='fa fa-spinner fa-spin fa-lg'></i><i ng-show=\"!searching\" class=\"fa fa-search\"></i>&nbsp;&nbsp;</button>\n" +
//    "                        <span data-ng-if=\"definitions\" class=\"dropdown\">\n" +
//    "                            <button class=\"btn btn-default navbar-btn dropdown-toggle\" data-toggle=\"dropdown\" data-target=\"new-form-dropdown\" id=\"new-form-button\" type=\"button\"><i class=\"fa fa-play-circle-o\"></i> <b class=\"caret\"></b></button>\n" +
//    "                            <ul id=\"new-form-dropdown\" class=\"dropdown-menu\" role=\"menu\" aria-labelledby=\"new-form-button\">\n" +
//    "                                <li data-ng-repeat=\"definition in definitions\" class=\"presentation\"><a role=\"menuitem\" href=\"{{definition.link}}\" target=\"_self\">{{definition.task.processDefinitionLabel}}</a></li>\n" +
//    "                            </ul>\n" +
//    "                        </span>\n" +
//    "                    </div>\n" +
//    "                    <div class=\"row\">\n" +
//    "                        <ul class=\"navbar-nav\">\n" +
//    "                            <li>\n" +
//    "                                <div class=\"dropdown\">\n" +
//    "                                    <a id=\"filter-button\" class=\"btn btn-link btn-small dropdown-toggle\" data-target=\"limit-dropdown\" data-toggle=\"dropdown\" role=\"button\" type=\"button\">\n" +
//    "                                        <span class=\"dropdown-toggle-text\">{{processStatusDescription[criteria.processStatus]}}</span>\n" +
//    "                                        <b class=\"caret\"></b>\n" +
//    "                                    </a>\n" +
//    "                                    <ul id=\"limit-dropdown\" class=\"dropdown-menu form-inline\" role=\"menu\" aria-labelledby=\"filter-button\">\n" +
//    "                                        <li role=\"presentation\" class=\"dropdown-header\">Process status</li>\n" +
//    "                                        <li role=\"presentation\" class=\"disabled\">\n" +
//    "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//    "                                                <label class=\"checkbox\">\n" +
//    "                                                    <input type=\"checkbox\" id=\"statusOpen\" data-ng-change=\"refreshSearch()\" data-ng-model=\"criteria.processStatus\" data-ng-true-value=\"open\" role=\"menuitem\" checked=\"\"/> &nbsp;{{processStatusDescription['open']}}\n" +
//    "                                                </label>\n" +
//    "                                            </div>\n" +
//    "                                        </li>\n" +
//    "                                        <li role=\"presentation\">\n" +
//    "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//    "                                                <label class=\"checkbox\">\n" +
//    "                                                    <input type=\"checkbox\" id=\"statusComplete\" data-ng-change=\"refreshSearch()\" data-ng-model=\"criteria.processStatus\" data-ng-true-value=\"complete\" role=\"menuitem\"> &nbsp;{{processStatusDescription['complete']}}\n" +
//    "                                                </label>\n" +
//    "                                            </div>\n" +
//    "                                        </li>\n" +
//    "                                        <li role=\"presentation\">\n" +
//    "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//    "                                                <label class=\"checkbox\">\n" +
//    "                                                    <input type=\"checkbox\" id=\"statusCancelled\" data-ng-change=\"refreshSearch()\" data-ng-model=\"criteria.processStatus\" data-ng-true-value=\"cancelled\" role=\"menuitem\"> &nbsp;{{processStatusDescription['cancelled']}}\n" +
//    "                                                </label>\n" +
//    "                                            </div>\n" +
//    "                                        </li>\n" +
//    "                                        <li role=\"presentation\">\n" +
//    "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//    "                                                <label class=\"checkbox\">\n" +
//    "                                                    <input type=\"checkbox\" id=\"statusSuspended\" data-ng-change=\"refreshSearch()\" data-ng-model=\"criteria.processStatus\" data-ng-true-value=\"suspended\" role=\"menuitem\"> &nbsp;{{processStatusDescription['suspended']}}\n" +
//    "                                                </label>\n" +
//    "                                            </div>\n" +
//    "                                        </li>\n" +
//        "                                        <li role=\"presentation\" class=\"disabled\">\n" +
//        "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//        "                                                <label class=\"checkbox\">\n" +
//        "                                                    <input type=\"checkbox\" id=\"statusQueued\" data-ng-change=\"refreshSearch()\" data-ng-model=\"criteria.processStatus\" data-ng-true-value=\"queued\" role=\"menuitem\" checked=\"\"/> &nbsp;{{processStatusDescription['queued']}}\n" +
//        "                                                </label>\n" +
//        "                                            </div>\n" +
//        "                                        </li>\n" +
//    "                                        <li role=\"presentation\">\n" +
//    "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//    "                                                <label class=\"checkbox\">\n" +
//    "                                                    <input type=\"checkbox\" id=\"statusAny\" data-ng-change=\"refreshSearch()\" data-ng-model=\"criteria.processStatus\" data-ng-true-value=\"all\" role=\"menuitem\"> &nbsp;{{processStatusDescription['all']}}\n" +
//    "                                                </label>\n" +
//    "                                            </div>\n" +
//    "                                        </li>\n" +
//    "                                    </ul>\n" +
//    "\n" +
//    "                                </div>\n" +
//    "                            </li>\n" +
//    "                            <li>\n" +
//    "                                <div class=\"dropdown\">\n" +
//    "                                    <a id=\"task-status-button\" class=\"btn btn-link btn-small dropdown-toggle\" data-target=\"limit-dropdown\" data-toggle=\"dropdown\" role=\"button\" type=\"button\">\n" +
//    "                                        <span class=\"dropdown-toggle-text\">{{taskStatusDescription[criteria.taskStatus]}}</span>\n" +
//    "                                        <b class=\"caret\"></b>\n" +
//    "                                    </a>\n" +
//    "                                    <ul class=\"dropdown-menu form-inline\" role=\"menu\" aria-labelledby=\"task-status-button\">\n" +
//    "                                        <li role=\"presentation\" class=\"dropdown-header\">Task status</li>\n" +
//    "                                        <li role=\"presentation\" class=\"disabled\">\n" +
//    "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//    "                                                <label class=\"checkbox\">\n" +
//    "                                                    <input type=\"checkbox\" data-ng-change=\"refreshSearch()\" data-ng-model=\"criteria.taskStatus\" data-ng-true-value=\"Open\" role=\"menuitem\" checked=\"\"/> &nbsp;{{taskStatusDescription['Open']}}\n" +
//    "                                                </label>\n" +
//    "                                            </div>\n" +
//    "                                        </li>\n" +
//    "                                        <li role=\"presentation\">\n" +
//    "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//    "                                                <label class=\"checkbox\">\n" +
//    "                                                    <input type=\"checkbox\" data-ng-change=\"refreshSearch()\" data-ng-model=\"criteria.taskStatus\" data-ng-true-value=\"Complete\" role=\"menuitem\"> &nbsp;{{taskStatusDescription['Complete']}}\n" +
//    "                                                </label>\n" +
//    "                                            </div>\n" +
//    "                                        </li>\n" +
//    "                                        <li role=\"presentation\">\n" +
//    "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//    "                                                <label class=\"checkbox\">\n" +
//    "                                                    <input type=\"checkbox\" data-ng-change=\"refreshSearch()\" data-ng-model=\"criteria.taskStatus\" data-ng-true-value=\"Cancelled\" role=\"menuitem\"> &nbsp;{{taskStatusDescription['Cancelled']}}\n" +
//    "                                                </label>\n" +
//    "                                            </div>\n" +
//    "                                        </li>\n" +
//    "                                        <li role=\"presentation\">\n" +
//    "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//    "                                                <label class=\"checkbox\">\n" +
//    "                                                    <input type=\"checkbox\" data-ng-change=\"refreshSearch()\" data-ng-model=\"criteria.taskStatus\" data-ng-true-value=\"Rejected\" role=\"menuitem\"> &nbsp;{{taskStatusDescription['Rejected']}}\n" +
//    "                                                </label>\n" +
//    "                                            </div>\n" +
//    "                                        </li>\n" +
//    "                                        <li role=\"presentation\">\n" +
//    "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//    "                                                <label class=\"checkbox\">\n" +
//    "                                                    <input type=\"checkbox\" data-ng-change=\"refreshSearch()\" data-ng-model=\"criteria.taskStatus\" data-ng-true-value=\"Suspended\" role=\"menuitem\"> &nbsp;{{taskStatusDescription['Suspended']}}\n" +
//    "                                                </label>\n" +
//    "                                            </div>\n" +
//    "                                        </li>\n" +
//    "                                        <li role=\"presentation\">\n" +
//    "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//    "                                                <label class=\"checkbox\">\n" +
//    "                                                    <input type=\"checkbox\" data-ng-change=\"refreshSearch()\" data-ng-model=\"criteria.taskStatus\" data-ng-true-value=\"all\" role=\"menuitem\"> &nbsp;{{taskStatusDescription['all']}}\n" +
//    "                                                </label>\n" +
//    "                                            </div>\n" +
//    "                                        </li>\n" +
//    "                                    </ul>\n" +
//    "                                </div>\n" +
//    "                            </li>\n" +
//    "                            <li ng-hide=\"isSingleProcessSelectable()\">\n" +
//    "                                <div class=\"dropdown\">\n" +
//    "                                    <a id=\"process-definition-button\" class=\"btn btn-link btn-small dropdown-toggle\" data-target=\"limit-dropdown\" data-toggle=\"dropdown\" role=\"button\" type=\"button\">\n" +
//    "                                        <span class=\"dropdown-toggle-text\">{{processDefinitionDescription[criteria.processDefinitionKey]}}</span>\n" +
//    "                                        <b class=\"caret\"></b>\n" +
//    "                                    </a>\n" +
//    "                                    <ul class=\"dropdown-menu form-inline\" role=\"menu\" aria-labelledby=\"process-definition-button\">\n" +
//    "                                        <li role=\"presentation\" class=\"dropdown-header\">Processes</li>\n" +
//    "                                        <li data-ng-repeat=\"definition in definitions\" role=\"presentation\">\n" +
//    "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//    "                                                <label class=\"checkbox\">\n" +
//    "                                                    <input type=\"checkbox\" data-ng-change=\"refreshSearch()\" data-ng-model=\"criteria.processDefinitionKey\" data-ng-true-value=\"{{definition.task.processDefinitionKey}}\" role=\"menuitem\" checked=\"\"/> &nbsp;{{definition.task.processDefinitionLabel}}\n" +
//    "                                                </label>\n" +
//    "                                            </div>\n" +
//    "                                        </li>\n" +
//    "                                        <li role=\"presentation\">\n" +
//    "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//    "                                                <label class=\"checkbox\">\n" +
//    "                                                    <input type=\"checkbox\" data-ng-change=\"refreshSearch()\" data-ng-model=\"criteria.processDefinitionKey\" data-ng-true-value=\"\" role=\"menuitem\"> &nbsp;{{processDefinitionDescription['']}}\n" +
//    "                                                </label>\n" +
//    "                                            </div>\n" +
//    "                                        </li>\n" +
//    "                                    </ul>\n" +
//    "                                </div>\n" +
//    "                            </li>\n" +
//    "                            <li>\n" +
//    "                                <div data-ng-hide=\"dates.isNonCustomDateRange()\">\n" +
//    "                                    <input type=\"datetime-local\" data-ng-model=\"dates.customStartedAfter\" data-ng-change=\"dates.refreshCustomDate()\" />\n" +
//    "                                    <input type=\"datetime-local\" data-ng-model=\"dates.customStartedBefore\" data-ng-change=\"dates.refreshCustomDate()\" />\n" +
//    "                                    <button type=\"button\" class=\"close\" data-ng-click=\"dates.selectDateRange('any')\" aria-hidden=\"true\" style=\"float:none;\">&times;</button>\n" +
//    "                                </div>\n" +
//    "                                <div data-ng-show=\"dates.isNonCustomDateRange()\" class=\"dropdown\">\n" +
//    "                                    <a id=\"date-limit-button\" class=\"btn btn-link btn-small dropdown-toggle\" data-target=\"limit-dropdown\" data-toggle=\"dropdown\" role=\"button\" type=\"button\">\n" +
//    "                                        <span class=\"dropdown-toggle-text\">{{dates.showNonCustomDateRange()}}</span>\n" +
//    "                                        <b class=\"caret\"></b>\n" +
//    "                                    </a>\n" +
//    "                                    <ul class=\"dropdown-menu form-inline\" role=\"menu\" aria-labelledby=\"process-definition-button\">\n" +
//    "                                        <li role=\"presentation\" class=\"dropdown-header\">Date range</li>\n" +
//    "                                        <li data-ng-repeat=\"dateRangeKey in dates.dateRangeKeys\" role=\"presentation\">\n" +
//    "                                            <div class=\"checkbox-menu-item\" role=\"menuitem\" tabindex=\"-1\">\n" +
//    "                                                <label class=\"checkbox\">\n" +
//    "                                                    <input type=\"checkbox\" data-ng-change=\"dates.selectDateRange(dateRangeKey)\" data-ng-model=\"dates.selectedDateRange\" data-ng-true-value=\"{{dateRangeKey}}\" role=\"menuitem\" checked=\"dates.selectedDateRangeKey == dateRangeKey\"/> &nbsp;{{dates.dateRanges[dateRangeKey]}}\n" +
//    "                                                </label>\n" +
//    "                                            </div>\n" +
//    "                                        </li>\n" +
//    "                                    </ul>\n" +
//    "                                </div>\n" +
//    "                            </li>\n" +
//    "                        </ul>\n" +
//    "                    </div>\n" +
//    "                </form>\n" +
//    "                <div class=\"navbar-right btn-toolbar wf-right-btn-toolbar\">\n" +
//    "                    <button data-ng-click=\"dialogs.openAssignModal(getFormsSelected(['Open']))\" data-ng-show=\"isFormSelected(['Open'])\" class=\"btn btn-default navbar-btn incomplete-selected-result-btn\" id=\"assign-dialog-button\" title=\"Assign task\" type=\"button\"><i class=\"fa fa-user fa-white\"></i></button>\n" +
//    "                    <button data-ng-click=\"dialogs.openHistoryModal(getFormsSelected())\" data-ng-show=\"isFormSelected()\" data-ng-disabled=\"!isSingleFormSelected()\" class=\"btn btn-default navbar-btn selected-result-btn\" id=\"history-dialog-button\" title=\"History\" type=\"button\"><i class=\"fa fa-calendar-o fa-white\"></i></button>\n" +
//    "                    <button data-ng-click=\"dialogs.openActivateModal(getFormsSelected(['Suspended']))\" data-ng-show=\"isFormSelected(['Suspended'])\" class=\"btn btn-default navbar-btn\" id=\"activate-dialog-button\" title=\"Activate process\" type=\"button\"><i class=\"fa fa-play fa-white\"></i></button>\n" +
//    "                    <button data-ng-click=\"dialogs.openSuspendModal(getFormsSelected(['Open']))\" data-ng-show=\"isFormSelected(['Open'])\" class=\"btn btn-default navbar-btn\" id=\"suspend-dialog-button\" title=\"Suspend process\" type=\"button\"><i class=\"fa fa-pause fa-white\"></i></button>\n" +
//    "                    <button data-ng-click=\"dialogs.openCancelModal(getFormsSelected(['Open','Suspended']))\" data-ng-show=\"isFormSelected(['Open','Suspended'])\" class=\"btn btn-danger navbar-btn incomplete-selected-result-btn\" id=\"delete-dialog-button\" title=\"Cancel process\" type=\"button\"><i class=\"fa fa-trash-o fa-white\"></i></button>\n" +
//    //"                    <button data-ng-click=\"dialogs.openRestartModal(getFormsSelected(['Cancelled', 'Complete', 'Rejected']))\" data-ng-show=\"isFormSelected(['Cancelled', 'Complete', 'Rejected'])\" class=\"btn btn-default navbar-btn\" title=\"Restart process\" type=\"button\"><i class=\"fa fa-rotate-left\"></i></button>\n" +
//    "                    <button data-ng-click=\"dialogs.openRestartModal(getFormsSelected())\" data-ng-show=\"isFormSelected()\" class=\"btn btn-default navbar-btn\" title=\"Restart process\" type=\"button\"><i class=\"fa fa-rotate-left\"></i></button>\n" +
//    "                    <a data-ng-show=\"false && !isFormSelected()\" href=\"report.html\" rel=\"external\" target=\"_self\" class=\"btn btn-default navbar-btn\" id=\"report-button\"><i class=\"fa fa-bar-chart-o\"></i></a>\n" +
//    "                    <button data-ng-click=\"exportCsv(getFormsSelected())\" data-ng-show=\"!isFormSelected()\" data-ng-disabled=\"!isSingleProcessSelected()\" class=\"btn btn-default navbar-btn\" title=\"Export as CSV\" type=\"button\"><i class=\"fa fa-download\"></i> Export</button>\n" +
//    "                </div>\n" +
//    "            </div>\n" +
//    "        </div>\n" +
//    "    </nav>");
//  $templateCache.put("templates/date.html",
//    '<div class="input-group wf-datepicker-group">' +
//    '   <input name="{{name}}" size="10" type="text" class="form-control wf-datepicker" datepicker-popup data-ng-model="date" datepicker-options="dateOptions"  is-open="opened" min="minDate" max="maxDate" close-text="Close" show-weeks="false"/>' +
//    '   <span class="input-group-addon">' +
//    '       <i class="fa fa-calendar"></i> ' +
//    '   </span> ' +
//    '</div>');
//  $templateCache.put("templates/daterange.html",
//    '<input data-ng-change="afterChange()" size="8" type="text" class="form-control wf-datepicker input-sm" datepicker-popup data-ng-model="after" datepicker-options="dateOptions"  is-open="afterOpened" min="afterMinDate" max="afterMaxDate" close-text="Close" placeholder="After" show-weeks="false"/>' +
//    '<span data-ng-click="clearFilter(facet)" data-ng-show="hasFilter(facet)" aria-hidden="true" class="form-control-feedback"><i class="fa fa-times-circle text-muted"></i></span> ' +
//    '<input data-ng-change="beforeChange()" size="8" type="text" class="form-control wf-datepicker input-sm" datepicker-popup data-ng-model="before" datepicker-options="dateOptions"  is-open="beforeOpened" min="beforeMinDate" max="beforeMaxDate" close-text="Close" placeholder="Before" show-weeks="false"/>' +
//    '<span data-ng-click="clearFilter(facet)" data-ng-show="hasFilter(facet)" aria-hidden="true" class="form-control-feedback"><i class="fa fa-times-circle text-muted"></i></span> ' +
//
////    '<input size="8" type="text" class="form-control wf-datepicker input-sm" datepicker-popup data-ng-model="before" is-open="beforeOpened" min="beforeMinDate" datepicker-options="dateOptions" date-disabled="beforeDisabled(date, mode)" ng-required="true" close-text="Close" placeholder="Before"/> ' +
//    '');
//  $templateCache.put("templates/searchresponse.html",
//    '       <div class="pull-right">{{paging.total}} task{{paging.total != 1 ? \'s\' : \'\'}}</div>' +
//    '       <h2 data-ng-bind="isSingleProcessSelected() ? processDefinitionDescription[criteria.processDefinitionKey] : \'&nbsp;\'"></h2>\n' +
//    '       <table class="table table-hover">\n' +
//    '            <thead>\n' +
//    '            <tr>' +
//    '               <th><input data-ng-click="selectAllForms(displayedForms)" data-ng-checked="allChecked" type="checkbox" class="result-checkbox"/></th>\n' +
////    '               <th class="text-muted"></th>' +
//    '               <th style="white-space:nowrap">' +
//    '                   <div class="form-group has-feedback">' +
//    '                       <label class="control-label"><a href="#" data-ng-click="doSort(facetMap[\'processInstanceLabel\'])"><b>Label</b> <i data-ng-show="isSorting(facetMap[\'processInstanceLabel\'])" data-ng-class="facetMap[\'processInstanceLabel\'].direction == \'asc\' ? \'fa-caret-up\' : \'fa-caret-down\'" class="fa"></i></a></label>' +
//    '                       <div data-ng-show="isFiltering" class="wf-filter">' +
//    '                           <input data-ng-keyup="onFilterKeyUp(facetMap[\'processInstanceLabel\'], $event)" data-ng-model="criteria[\'processInstanceLabel\']" autocomplete="off" type="text" class="form-control input-sm natural" placeholder="Label">\n' +
//    '                           <span data-ng-click="clearFilter(facetMap[\'processInstanceLabel\'])" data-ng-show="hasFilter(facetMap[\'processInstanceLabel\'])" aria-hidden="true" class="form-control-feedback"><i class="fa fa-times-circle text-muted"></i></span>\n' +
//    '                       </div>' +
//    '                    </div>' +
//    '               </th>' +
//    '               <th data-ng-class="facet.required ? \'\' : \'hidden-sm hidden-xs\'" data-ng-repeat="facet in selectedFacets" style="white-space:nowrap">' +
//    '                   <div data-ng-class="facet.type !== \'date\' ? \'has-feedback\' : \'\'" class="form-group">\n' +
//    '                       <label class="control-label"><a href="#" data-ng-click="doSort(facet)"><b>{{facet.label}}</b> <i data-ng-show="isSorting(facet)" data-ng-class="facet.direction == \'asc\' ? \'fa-caret-up\' : \'fa-caret-down\'" class="fa"></i></a></label>\n' +
//    '                       <div data-ng-show="isFiltering" class="wf-filter">' +
//    '                           <div data-ng-show="facet.type == \'date\' || facet.type == \'datetime\'"> ' +
//    '                               <div data-wf-date-range data-name="facet.name" data-criteria="criteria" />' +
//    '                           </div> ' +
//    '                           <input data-ng-keyup="onFilterKeyUp(facet, $event)" data-ng-hide="facet.type == \'date\' || facet.type == \'datetime\'" data-ng-model="criteria[facet.name]" autocomplete="off" type="text" class="form-control input-sm natural" placeholder="{{facet.label}}">\n' +
//    '                           <span data-ng-click="clearFilter(facet)" data-ng-show="hasFilter(facet)" aria-hidden="true" class="form-control-feedback"><i class="fa fa-times-circle text-muted"></i></span>\n' +
//    '                       </div>' +
//    '                   </div>' +
//    '               </th>' +
//    '            </tr>\n' +
//    '            </thead>\n' +
//    '            <tbody>\n' +
//    '            <tr data-ng-repeat="form in displayedForms">\n' +
//    '                <td><input data-ng-click="selectForm(form)" data-ng-checked="form.checked" type="checkbox" class="result-checkbox"/></td>\n' +
////    '                <td class="text-muted">{{form.itemNumber}}</td>' +
//    '                  <td><a href="{{form.link}}" target="_self" rel="external">{{form.processInstanceLabel}}</a></td>' +
//    '                  <td data-ng-class="facet.required ? \'\' : \'hidden-sm hidden-xs\'" data-ng-repeat="facet in selectedFacets">{{form[facet.name]}}</td>' +
////    '                <td data-ng-class="facet.required ? \'\' : \'hidden-sm hidden-xs\'" data-ng-show="facet.selected" data-ng-repeat="facet in facets">' +
////    '                   <span>{{form[facet.name]}}</span>' +
////    '                   <span data-ng-show="facet.link"><a href="{{form.link}}" target="_self" rel="external">{{getFacetValue(form, facet)}}</a></span>' +
////    '                   <span data-ng-hide="facet.link">{{getFacetValue(form, facet)}}</span>' +
////    '                </td>' +
//    '            </tr>\n' +
//    '            </tbody>' +
//    '           \n' +
//    '       </table>' +
//    '       <ul data-ng-show="paging.required" class="pagination pull-right"> ' +
//    '           <li><a data-ng-click="paging.previousPage()">&larr; Previous</a></li> ' +
//    '           <li data-ng-class="pageNumber == paging.pageNumber ? \'active\' : \'\'" data-ng-repeat="pageNumber in paging.pageNumbers"><a data-ng-click="paging.toPage(pageNumber)">{{pageNumber}}</a></li> ' +
//    '           <li><a data-ng-click="paging.nextPage()">Next &rarr;</a></li> ' +
//    '       </ul>' +
//
////    '       <select data-ng-model="paging.pageSize" data-ng-change="paging.changePageSize($event)" class="form-control">' +
////    '           <option value="10">10</option>' +
////    '           <option value="100">100</option>' +
////    '           <option value="1000">1000</option>' +
////    '           <option value="10000">10,000</option>' +
////    '       </select>'
//    '');

//  $templateCache.put("templates/searchresults.html",
//    "<h2 data-ng-bind=\"isSingleProcessSelected() ? processDefinitionDescription[criteria.processDefinitionKey] : 'Tasks'\"></h2>\n" +
//    "        <table class=\"search-results table table-hover\">\n" +
//    "            <thead>\n" +
//    "            <tr>\n" +
//    "                <th><i class=\"icon-check\"></i></th>\n" +
//    "                <th>Label</th>\n" +
//    "                <th class=\"hidden-sm hidden-xs\">Status</th>\n" +
//    "                <th class=\"hidden-sm hidden-xs\">Task</th>\n" +
//    "                <th class=\"hidden-xs\">Assignee</th>\n" +
//    "                <th>Date</th>\n" +
//    "            </tr>\n" +
//    "            </thead>\n" +
//    "            <tbody>\n" +
//    "            <tr data-ng-repeat=\"form in forms\">\n" +
//    "                <td><input data-ng-click=\"selectForm(form)\" type=\"checkbox\" class=\"result-checkbox\"/></td>\n" +
//    "                <td>\n" +
//    "                    <a href=\"{{form.link}}\" target=\"_self\" rel=\"external\">{{form.task.processInstanceLabel}}</a>\n" +
//    "                </td>\n" +
//    "                <td class=\"hidden-sm hidden-xs\">{{form.task.taskStatus}}</td>\n" +
//    "                <td class=\"hidden-sm hidden-xs\" style=\"cursor:pointer\"><span class=\"use-tooltip\" title=\"{{form.task.taskDescription}}\" data-placement=\"right\" data-trigger=\"hover\">{{form.task.taskLabel}}</span></td>\n" +
//    "                <td class=\"hidden-xs\">{{form.task.assignee ? form.task.assignee.displayName : 'Nobody'}}</td>\n" +
//    "                <td>{{form.task.startTime|date:'MMM d, y H:mm'}}<span data-ng-if=\"form.task.endTime\"> - {{form.task.endTime|date:'MMM d, y H:mm'}}</span></td>\n" +
//    "            </tr>\n" +
//    "            </tbody>\n" +
//    "        </table>");
//   $templateCache.put("templates/searchtoolbar.html",
//    '<nav class="navbar navbar-default navbar-ex1-collapse" style="margin-bottom: 0px;border-radius: 0px">\n' +
//    '        <div class="navbar-header">\n' +
//    '            <button data-ng-click="state.toggleCollapse()" type="button" class="navbar-toggle">\n' +
//    '                <span class="sr-only">Toggle</span>\n' +
//    '                <span class="icon-bar"></span>\n' +
//    '                <span class="icon-bar"></span>\n' +
//    '                <span class="icon-bar"></span>\n' +
//    '            </button>\n' +
//    '        </div>\n' +
//    '        <div data-ng-class="state.isCollapsed ? \'\' : \'collapse\'" class="navbar-collapse navbar-ex1-collapse">\n' +
//    '            <div class="container">\n' +
//    '                <div class="row"><form class="navbar-form navbar-left form-inline" role="search">\n' +
//    '                    <div class="row">\n' +
//    '                       <div class="form-group has-feedback">\n' +
//    '                           <input data-ng-keyup="onSearchKeyUp($event)" style="width: 400px" title="Search by keyword" role="" class="form-control searchField" data-ng-model="criteria.keywords" placeholder="Search" id="keyword" type="text">\n' +
//    '                           <span data-ng-click="clearSearch()" data-ng-show="criteria.keywords" aria-hidden="true" class="form-control-feedback"><i class="fa fa-times-circle text-muted"></i></span>\n' +
//    '                       </div>' +
//    '                       <button data-ng-click="refreshSearch()" class="btn btn-default navbar-btn" role="button" id="instanceSearchButton" type="submit">&nbsp;&nbsp;<i data-ng-class="searching ? \'fa-spinner fa-spin\' : \'fa-search\'" id="searchIcon" class="fa fa-lg"></i>&nbsp;&nbsp;</button>\n' +
//    '                       <span data-ng-if="definitions" class="dropdown">\n' +
//    '                            <button class="btn btn-default navbar-btn dropdown-toggle" data-toggle="dropdown" data-target="new-form-dropdown" id="new-form-button" type="button"><i class="fa fa-play-circle-o"></i> <b class="caret"></b></button>\n' +
//    '                            <ul id="new-form-dropdown" class="dropdown-menu" role="menu" aria-labelledby="new-form-button">\n' +
//    '                                <li data-ng-repeat="definition in definitions" class="presentation"><a role="menuitem" href="{{definition.link}}" target="_self">{{definition.processDefinitionLabel}}</a></li>\n' +
//    '                            </ul>\n' +
//    '                       </span>\n' +
//    '                    </div>\n' +
//    '                    <div class="row">\n' +
//    '                        <ul class="navbar-nav">\n' +
//    '                            <li>\n' +
//    '                                <div class="dropdown">\n' +
//    '                                    <a id="filter-button" class="btn btn-link btn-small dropdown-toggle" data-target="limit-dropdown" data-toggle="dropdown" role="button" type="button">\n' +
//    '                                        <span class="dropdown-toggle-text">{{processStatusDescription[criteria.processStatus]}}</span>\n' +
//    '                                        <b class="caret"></b>\n' +
//    '                                    </a>\n' +
//    '                                    <ul id="limit-dropdown" class="dropdown-menu form-inline" role="menu" aria-labelledby="filter-button">\n' +
//    '                                        <li role="presentation" class="dropdown-header">Process status</li>\n' +
//    '                                        <li role="presentation" class="disabled">\n' +
//    '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
//    '                                                <label class="checkbox">\n' +
//    '                                                    <input type="checkbox" id="statusOpen" data-ng-change="refreshSearch()" data-ng-model="criteria.processStatus" data-ng-true-value="open" role="menuitem" checked=""/> &nbsp;{{processStatusDescription[\'open\']}}\n' +
//    '                                                </label>\n' +
//    '                                            </div>\n' +
//    '                                        </li>\n' +
//    '                                        <li role="presentation">\n' +
//    '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
//    '                                                <label class="checkbox">\n' +
//    '                                                    <input type="checkbox" id="statusComplete" data-ng-change="refreshSearch()" data-ng-model="criteria.processStatus" data-ng-true-value="complete" role="menuitem"> &nbsp;{{processStatusDescription[\'complete\']}}\n' +
//    '                                                </label>\n' +
//    '                                            </div>\n' +
//    '                                        </li>\n' +
//    '                                        <li role="presentation">\n' +
//    '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
//    '                                                <label class="checkbox">\n' +
//    '                                                    <input type="checkbox" id="statusCancelled" data-ng-change="refreshSearch()" data-ng-model="criteria.processStatus" data-ng-true-value="cancelled" role="menuitem"> &nbsp;{{processStatusDescription[\'cancelled\']}}\n' +
//    '                                                </label>\n' +
//    '                                            </div>\n' +
//    '                                        </li>\n' +
//    '                                        <li role="presentation">\n' +
//    '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
//    '                                                <label class="checkbox">\n' +
//    '                                                    <input type="checkbox" id="statusSuspended" data-ng-change="refreshSearch()" data-ng-model="criteria.processStatus" data-ng-true-value="suspended" role="menuitem"> &nbsp;{{processStatusDescription[\'suspended\']}}\n' +
//    '                                                </label>\n' +
//    '                                            </div>\n' +
//    '                                        </li>\n' +
//    '                                        <li role="presentation" class="disabled">\n' +
//    '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
//    '                                                <label class="checkbox">\n' +
//    '                                                    <input type="checkbox" id="statusQueued" data-ng-change="refreshSearch()" data-ng-model="criteria.processStatus" data-ng-true-value="queued" role="menuitem" checked=""/> &nbsp;{{processStatusDescription[\'queued\']}}\n' +
//    '                                                </label>\n' +
//    '                                            </div>\n' +
//    '                                        </li>\n' +
//    '                                        <li role="presentation">\n' +
//    '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
//    '                                                <label class="checkbox">\n' +
//    '                                                    <input type="checkbox" id="statusAny" data-ng-change="refreshSearch()" data-ng-model="criteria.processStatus" data-ng-true-value="all" role="menuitem"> &nbsp;{{processStatusDescription[\'all\']}}\n' +
//    '                                                </label>\n' +
//    '                                            </div>\n' +
//    '                                        </li>\n' +
//    '                                    </ul>\n' +
//    '                                </div>\n' +
//    '                            </li>\n' +
//    '                            <li ng-hide="isSingleProcessSelectable()">\n' +
//    '                                <div class="dropdown">\n' +
//    '                                    <a id="process-definition-button" class="btn btn-link btn-small dropdown-toggle" data-target="limit-dropdown" data-toggle="dropdown" role="button" type="button">\n' +
//    '                                        <span class="dropdown-toggle-text">{{processDefinitionDescription[criteria.processDefinitionKey]}}</span>\n' +
//    '                                        <b class="caret"></b>\n' +
//    '                                    </a>\n' +
//    '                                    <ul class="dropdown-menu form-inline" role="menu" aria-labelledby="process-definition-button">\n' +
//    '                                        <li role="presentation" class="dropdown-header">Processes</li>\n' +
//    '                                        <li data-ng-repeat="definition in definitions" role="presentation">\n' +
//    '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
//    '                                                <label class="checkbox">\n' +
//    '                                                    <input type="checkbox" data-ng-change="refreshSearch()" data-ng-model="criteria.processDefinitionKey" data-ng-true-value="{{definition.processDefinitionKey}}" role="menuitem" checked=""/> &nbsp;{{definition.processDefinitionLabel}}\n' +
//    '                                                </label>\n' +
//    '                                            </div>\n' +
//    '                                        </li>\n' +
//    '                                        <li role="presentation">\n' +
//    '                                            <div class="checkbox-menu-item" role="menuitem" tabindex="-1">\n' +
//    '                                                <label class="checkbox">\n' +
//    '                                                    <input type="checkbox" data-ng-change="refreshSearch()" data-ng-model="criteria.processDefinitionKey" data-ng-true-value="" role="menuitem"> &nbsp;{{processDefinitionDescription[\'\']}}\n' +
//    '                                                </label>\n' +
//    '                                            </div>\n' +
//    '                                        </li>\n' +
//    '                                    </ul>\n' +
//    '                                </div>\n' +
//    '                            </li>\n' +
//    '                        </ul>\n' +
//    '                    </div>\n' +
//    '                </form>\n' +
//    '                <div class="navbar-right btn-toolbar">\n' +
////    '                    <div data-ng-show="isFormSelected([\'Open\'])" data-wf-assignment-button data-forms="getFormsSelected([\'Open\'])" class="navbar-nav"></div>' +
//    '                    <button data-ng-click="dialogs.openAssignModal(getFormsSelected([\'Open\']))" data-ng-show="isFormSelected([\'Open\'])" class="btn btn-default navbar-btn incomplete-selected-result-btn" id="assign-dialog-button" title="Assign task" type="button"><i class="fa fa-user fa-white"></i></button>\n' +
//    '                    <button data-ng-click="dialogs.openHistoryModal(getFormsSelected())" data-ng-show="isFormSelected()" data-ng-disabled="!isSingleFormSelected()" class="btn btn-default navbar-btn selected-result-btn" id="history-dialog-button" title="History" type="button"><i class="fa fa-calendar-o fa-white"></i></button>\n' +
//    '                    <button data-ng-click="dialogs.openActivateModal(getFormsSelected([\'Suspended\']))" data-ng-show="isFormSelected([\'Suspended\'])" class="btn btn-default navbar-btn" id="activate-dialog-button" title="Activate process" type="button"><i class="fa fa-play fa-white"></i></button>\n' +
//    '                    <button data-ng-click="dialogs.openSuspendModal(getFormsSelected([\'Open\']))" data-ng-show="isFormSelected([\'Open\'])" class="btn btn-default navbar-btn" id="suspend-dialog-button" title="Suspend process" type="button"><i class="fa fa-pause fa-white"></i></button>\n' +
//    '                    <button data-ng-click="dialogs.openCancelModal(getFormsSelected([\'Open\',\'Suspended\']))" data-ng-show="isFormSelected([\'Open\',\'Suspended\'])" class="btn btn-danger navbar-btn incomplete-selected-result-btn" id="delete-dialog-button" title="Cancel process" type="button"><i class="fa fa-trash-o fa-white"></i></button>\n' +
//    '                    <button data-ng-click="dialogs.openRestartModal(getFormsSelected())" data-ng-show="isFormSelected()" class="btn btn-default navbar-btn" title="Restart process" type="button"><i class="fa fa-rotate-left"></i></button>\n' +
//    '                    <a data-ng-show="false && !isFormSelected()" href="report.html" rel="external" target="_self" class="btn btn-default navbar-btn" id="report-button"><i class="fa fa-bar-chart-o"></i></a>\n' +
//    '                    <button data-ng-click="exportCsv(getFormsSelected())" data-ng-show="!isFormSelected()" data-ng-disabled="!isSingleProcessSelected()" class="btn btn-default navbar-btn" title="Export as CSV" type="button"><i class="fa fa-download"></i> Export</button>\n' +
//    '                    <button data-ng-click="toggleColumns()" data-ng-show="!isFormSelected()" class="btn btn-default navbar-btn"><i class="fa fa-columns fa-1x"></i></button>' +
//    '                    <button data-ng-click="toggleFilter()" data-ng-show="!isFormSelected()" class="btn btn-default navbar-btn">' +
//    '                       <i data-ng-class="isFiltering ? \'fa-ban\' : \'fa-filter\'" class="fa"></i>' +
//    '                    </button>' +
//    '                    <span data-ng-if="bucketList.buckets.length > 0" class="dropdown">\n' +
//    '                        <button class="btn btn-default navbar-btn dropdown-toggle" data-ng-disabled="!isFormSelected()" data-toggle="dropdown" data-target="new-form-dropdown" id="new-form-button" type="button"><i class="fa fa-tag"></i><b class="caret"></b></button>\n' +
//    '                        <ul id="new-form-dropdown" class="dropdown-menu scroll" role="menu" aria-labelledby="new-form-button">\n' +
//    '                            <li class="presentation" role="menuitem"><b>&nbsp;&nbsp;&nbsp;&nbsp;Change Bucket</b></li>\n' +
//    '                            <li data-ng-repeat="bucket in bucketList.buckets" data-ng-click="changeBucket(getFormsSelected(), bucket)" class="presentation" role="menuitem"><a>{{bucket}}</a></li>\n' +
//    '                        </ul>\n' +
//    '                    </span>\n ' +
//    '                </div>\n' +
//    '            </div></div>\n' +
//    '        </div>\n' +
//    '    </nav>');
  $templateCache.put("templates/toolbar.html",
    "<div class=\"btn-toolbar pull-right\" role=\"toolbar\">\n" +
    "        <button data-ng-if=\"!container.readonly\" data-ng-class=\"button.primary && 'btn-primary'\" data-ng-repeat=\"button in container.buttons\" data-ng-click=\"wizard.clickButton(form, container, button)\" class=\"btn btn-default\" name=\"{{button.name}}\" type=\"{{button.type}}\" value=\"{{button.value}}\">{{button.label}}</button>\n" +
    "    </div>");
}]);

define([
        'chaplin',
        'views/form/attachment-view',
        'views/base/collection-view',
        ],
    function(Chaplin, AttachmentView, CollectionView) {
	'use strict';

	var AttachmentsView = CollectionView.extend({
		autoRender: true,
		className: 'attachments span3',
        container: '.main-content',
		tagName: 'ul',
        itemView: AttachmentView
	});

	return AttachmentsView;
});
define([
        'chaplin',
        'views/form/attachment-view',
        'views/base/collection-view',
        ],
    function(Chaplin, AttachmentView, CollectionView) {
	'use strict';

	var AttachmentsView = CollectionView.extend({
		autoRender: true,
		className: 'attachments col-lg-3 col-sm-3',
        container: '.main-content',
        fallbackSelector: '.attachment-fallback',
		tagName: 'ul',
        itemView: AttachmentView,
        listen: {
            'refreshAttachments mediator': '_onRefreshAttachments',
        },
//        render: function() {
////            this.$el.append('<div class="attachment-fallback hide">No items</div>');
//            CollectionView.__super__.render.apply(this);
//            return this;
//        }
        _onRefreshAttachments: function() {
            this.collection.fetch();
        },
	});

	return AttachmentsView;
});
define([
        'chaplin',
        'models/design/section',
        'views/base/collection-view',
        'views/form/section-view',
        ],
    function(Chaplin, Section, CollectionView, SectionView) {
	'use strict';

	var SectionsView = CollectionView.extend({
		autoRender: true,
		className: 'sections',
		container: ".screen-content",
		tagName: 'ul',
		itemView: SectionView,
	});

	return SectionsView;
});
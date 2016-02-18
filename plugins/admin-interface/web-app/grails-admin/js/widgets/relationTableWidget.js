app.view('relationTableWidget', ['$el', 'relationPopupWidgetList', 'templateService'], function ($el, relationPopupWidgetList, templateService) {
    "use strict";

    var propertyName = $el.find("table").data('property-name');
    var optional = $el.find("table").data('optional');
    var detailUrl = $el.find("table").data('detailurl');

    var table = $el.find("tbody");

    if (!table.length) {
        table = $el.find("table");
    }

    function addItem (objectId, objectText){

        var url = detailUrl.replace("0", objectId);
        var newLine = createRelationTableWidgetLine(url, objectId, objectText, optional);

        table.append(newLine);

        $("<input>")
            .attr({
                'type': 'hidden',
                'name': propertyName,
                'value': objectId
            })
            .prependTo($el)

    }

    function createRelationTableWidgetLine (detailUrl, val, txt, optional) {
        return templateService.get('grails-admin-selected-item', {
            detailUrl: detailUrl,
            val: val,
            txt: txt,
            optional: optional
        });
    }

    function deleteRelation (event) {
        event.preventDefault();

        var r = confirm( "Do you wish to delete the relation?" );

        if (r == true) {
            $el.find("input[type='hidden'][value=" + $(this).data('value') + "]").remove();
            $(this).closest( "tr" ).remove()

        }
    }

    function addRelation (page) {
        page = page || 0;

        var target = $(this).data("target");


        var url_list = $(this).data('url');
        var url_count = $(this).data('url-count');
        var excludeValues = $el.find("input[type='hidden']")
            .map(function() {
                return parseInt($(this).val());
            }).toArray();

        relationPopupWidgetList
            .open("Select", excludeValues, url_list, url_count)
            .done(addItem);
    }

    function openNewPopup (event) {
        var target = $(event.currentTarget).data('target');
        $(target).trigger('grailsadmin:relationPopupWidgetNew', addItem);
    }

    $el.on( "click", ".js-relationtablewidget-delete", deleteRelation);
    $el.find( ".js-relationtablewidget-list").on("click", addRelation);
    $el.find(".js-relationtablewidget-new").on('click', openNewPopup);
});

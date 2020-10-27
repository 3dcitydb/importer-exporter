package org.citydb.plugin.cli;

import org.citydb.config.geometry.BoundingBox;
import org.citydb.config.project.query.QueryConfig;
import org.citydb.config.project.query.filter.selection.SelectionFilter;
import org.citydb.config.project.query.filter.selection.spatial.BBOXOperator;
import picocli.CommandLine;

public class QueryOption implements CliOption {
    @CommandLine.Mixin
    private TypeNamesOption typeNamesOption;

    @CommandLine.Mixin
    BoundingBoxOption boundingBoxOption;

    @CommandLine.Mixin
    private XMLQueryOption xmlQueryOption;

    public QueryConfig toQueryConfig() {
        if (xmlQueryOption.isSpecified()) {
            return xmlQueryOption.toQueryConfig();
        } else {
            QueryConfig queryConfig = new QueryConfig();

            if (typeNamesOption.isSpecified()) {
                queryConfig.setFeatureTypeFilter(typeNamesOption.toFeatureTypeFilter());
            }

            if (boundingBoxOption.isSpecified()) {
                BoundingBox bbox = boundingBoxOption.toBoundingBox();
                if (bbox != null) {
                    BBOXOperator bboxOperator = new BBOXOperator();
                    bboxOperator.setEnvelope(bbox);
                    SelectionFilter selectionFilter = new SelectionFilter();
                    selectionFilter.setPredicate(bboxOperator);
                    queryConfig.setSelectionFilter(selectionFilter);
                }
            }

            return queryConfig.isSetFeatureTypeFilter()
                    || queryConfig.isSetSelectionFilter() ?
                    queryConfig :
                    null;
        }
    }

    @Override
    public boolean isSpecified() {
        return typeNamesOption.isSpecified()
                || boundingBoxOption.isSpecified()
                || xmlQueryOption.isSpecified();
    }

    @Override
    public void preprocess(CommandLine commandLine) throws Exception {
        if (xmlQueryOption.isSpecified()) {
            if (typeNamesOption.isSpecified()) {
                throw new CommandLine.ParameterException(commandLine,
                        "Options --type-names and --xml-query are mutually exclusive.");
            }

            if (boundingBoxOption.isSpecified()) {
                throw new CommandLine.ParameterException(commandLine,
                        "Options --bbox and --xml-query are mutually exclusive.");
            }

            xmlQueryOption.preprocess(commandLine);
        }

        if (typeNamesOption.isSpecified()) {
            typeNamesOption.preprocess(commandLine);
        }

        if (boundingBoxOption.isSpecified()) {
            boundingBoxOption.preprocess(commandLine);
        }
    }
}

/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import React, {Component} from 'react';
import ListViewParent from '../common/ListViewParent';
import ResourceAPI from '../utils/apis/ResourceAPI';

import MUIDataTable from "mui-datatables";

export default class TemplateListPage extends Component {

    constructor(props) {
        super(props);
        this.endpointTemplates = null;
        this.sequenceTemplates = null;
        this.state = {
            templateData: []
        };
    }

    /**
     * Retrieve local entries from the MI.
     */
    componentDidMount() {
        this.retrieveLocalEntries();
    }

    retrieveLocalEntries() {
        const templateData = [];
        new ResourceAPI().getResourceList(`/templates`).then((response) => {
            this.endpointTemplates = response.data.endpointTemplateList || [];
            this.endpointTemplates.forEach((element) => {
                const endpointTemplateRowData = [];
                endpointTemplateRowData.push(element.name);
                endpointTemplateRowData.push('Endpoint Template');
                templateData.push(endpointTemplateRowData);

            });

            this.sequenceTemplates = response.data.sequenceTemplateList || [];
            this.sequenceTemplates.forEach((element) => {
                const sequenceTemplateRowData = [];
                sequenceTemplateRowData.push(element.name);
                sequenceTemplateRowData.push('Sequence Template');
                templateData.push(sequenceTemplateRowData);

            });
            this.setState({
                templateData: templateData
            });

        }).catch((error) => {
            //Handle errors here
        });
    }

    renderResourceList() {

        const columns = ["Template Name", "Type"];
        const options = {
            selectableRows: 'none',
            print: false,
            download: false,
        };

        return (
            <MUIDataTable
                title={"Templates"}
                data={this.state.templateData}
                columns={columns}
                options={options}
            />
        );
    }

    render() {
        return (
            <ListViewParent
                data={this.renderResourceList()}
            />
        );
    }
}

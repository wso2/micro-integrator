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

import React, { Component } from 'react';
import ListViewParent from '../common/ListViewParent';
import ResourceAPI from '../utils/apis/ResourceAPI';

import MUIDataTable from "mui-datatables";

export default class MessageStoreListPage extends Component {

    constructor(props) {
        super(props);
        this.messageStores = null;
        this.state = {
            data: [],
        };
    }

    /**
     * Retrieve message stores from the MI.
     */
    componentDidMount() {
        this.retrieveMessageStores();
    }

    retrieveMessageStores() {
        const data = [];

        new ResourceAPI().getResourceList(`/message-stores`).then((response) => {
            this.messageStores = response.data.list || [];

            this.messageStores.forEach((element) => {
                const rowData = [];
                rowData.push(element.name);
                rowData.push(element.type);
                data.push(rowData);

            });
            this.setState({data: data});

        }).catch((error) => {
            //Handle errors here
        });
        console.log(this.state.data);
    }

    renderResourceList() {

        const columns = ["Message Store Name", "Type"];
        const options = {
            selectableRows: 'none'
        };

        return (
            <MUIDataTable
                title={"Message Stores"}
                data={this.state.data}
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
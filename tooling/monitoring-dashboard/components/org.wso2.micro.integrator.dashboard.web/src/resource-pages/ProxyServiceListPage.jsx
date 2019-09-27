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
import Link from '@material-ui/core/Link';

import MUIDataTable from "mui-datatables";

export default class ProxyServiceListPage extends Component {

    constructor(props) {
        super(props);
        this.proxies = null;
        this.state = {
            data: [],
        };
    }

    /**
     * Retrieve proxies from the MI.
     */
    componentDidMount() {
        this.retrieveProxies();
    }

    retrieveProxies() {
        const data = [];

        new ResourceAPI().getResourceList(`/proxy-services`).then((response) => {
            this.proxies = response.data.list || [];

            this.proxies.forEach((element) => {
                const rowData = [];
                rowData.push(element.name);
                rowData.push(element.wsdl1_1);
                rowData.push(element.wsdl2_0);
                data.push(rowData);

            });
            this.setState({data: data});

        }).catch((error) => {
            //Handle errors here
        });
        console.log(this.state.data);
    }

    renderResourceList() {

        const columns = [
            {
                name: "Service",
                options: {
                    customBodyRender: (value, tableMeta, updateValue) => {
                        return (
                            <Link component="button" variant="body2" onClick={() => {
                                this.props.history.push(`/proxy/explore?name=${tableMeta.rowData[0]}`)
                            }}>
                                {tableMeta.rowData[0]}
                            </Link>
                        );
                    }
                }
            }, "WSDL1.1", "WSDL2.0"];
        const options = {
            selectableRows: 'none',
            print: false,
            download: false,
        };

        return (
            <MUIDataTable
                title={"Proxy Services"}
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

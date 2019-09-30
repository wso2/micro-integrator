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
import ResourceExplorerParent from '../common/ResourceExplorerParent';
import ResourceAPI from '../utils/apis/ResourceAPI';
import queryString from 'query-string'
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import TableHeaderBox from '../common/TableHeaderBox';
import SourceViewComponent from '../common/SourceViewComponent';
import Typography from '@material-ui/core/Typography';
import Breadcrumbs from '@material-ui/core/Breadcrumbs';
import { Link } from "react-router-dom";

import Box from '@material-ui/core/Box';

export default class MessageStoreDetailsPage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            metaData: [],
            parameters: {},
            response: {}
        };
    }

    /**
     * Retrieve message-store details from the MI.
     */
    componentDidMount() {
        let url = this.props.location.search;
        const values = queryString.parse(url) || {};
        this.retrieveMessageStoreInfo(values.name);
    }

    createData(name, value) {
        return {name, value};
    }

    retrieveMessageStoreInfo(name) {
        const metaData = [];
        new ResourceAPI().getMessageStoreServiceByName(name).then((response) => {

            metaData.push(this.createData("Store Name", response.data.name));
            metaData.push(this.createData("Size", response.data.size));
            const parameters = response.data.properties || {};
            this.setState(
                {
                    metaData: metaData,
                    parameters: parameters,
                    response: response.data
                });

        }).catch((error) => {
            //Handle errors here
        });
    }

    renderMessageStoreDetails() {
        return (
            <Box>
                <Box pb={5}>
                    <TableHeaderBox title="Store Details"/>
                    <Table size="small">
                        <TableBody>
                            {
                                this.state.metaData.map(row => (
                                    <TableRow>
                                        <TableCell>{row.name}</TableCell>
                                        <TableCell>{row.value}</TableCell>
                                    </TableRow>
                                ))
                            }
                        </TableBody>
                    </Table>
                </Box>

                <Box pb={5}>
                    <TableHeaderBox title="Parameters"/>
                    <Table size="small">
                        <TableBody>
                            {
                                Object.keys(this.state.parameters).map(key => (
                                    <TableRow>
                                        <TableCell>{key}</TableCell>
                                        <TableCell>{this.state.parameters[key]}</TableCell>
                                    </TableRow>
                                ))
                            }
                        </TableBody>
                    </Table>
                </Box>
                <SourceViewComponent config={this.state.response.configuration}/>
            </Box>
        );
    }

    renderBreadCrumbs() {
        return (
            <Breadcrumbs separator="›" aria-label="breadcrumb">
                <Box color="inherit" component={Link} to="/message-store" fontSize={14}>
                    Message Stores
                </Box>
                <Box color="textPrimary" fontSize={14}>{this.state.response.name}</Box>
            </Breadcrumbs>);
    }

    render() {
        console.log(this.state.config);
        return (
            <ResourceExplorerParent title={this.state.response.name + " Explorer"}
                                    content={this.renderMessageStoreDetails()} breadcrumb={this.renderBreadCrumbs()}/>
        );
    }
}
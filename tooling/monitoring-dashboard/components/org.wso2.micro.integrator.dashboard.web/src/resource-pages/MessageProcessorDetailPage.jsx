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
import Box from '@material-ui/core/Box';
import Breadcrumbs from '@material-ui/core/Breadcrumbs';
import {Link} from "react-router-dom";

export default class MessageProcessorDetailPage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            metaData: [],
            parameters: {},
            response: {},
        };
    }

    /**
     * Retrieve message-store details from the MI.
     */
    componentDidMount() {
        let url = this.props.location.search;
        const values = queryString.parse(url) || {};
        this.retrieveMessageProcessorInfo(values.name);
    }

    createData(name, value) {
        return {name, value};
    }

    retrieveMessageProcessorInfo(name) {
        const metaData = [];
        new ResourceAPI().getMessageProcessorByName(name).then((response) => {

            metaData.push(this.createData("Processor Name", response.data.name));
            metaData.push(this.createData("Message Store", response.data.messageStore));
            metaData.push(this.createData("Type", response.data.type));
            const parameters = response.data.parameters || {};
            this.setState(
                {
                    metaData: metaData,
                    parameters: parameters,
                    response: response.data,
                });

        }).catch((error) => {
            //Handle errors here
        });
    }

    renderMessageProcessorDetails() {
        return (
            <Box>
                <Box pb={5}>
                    <TableHeaderBox title="Processor Details"/>
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
                <Box color="inherit" component={Link} to="/message-processor" fontSize={14}>
                    Message Processors
                </Box>
                <Box color="textPrimary" fontSize={14}>{this.state.response.name}</Box>
            </Breadcrumbs>);
    }

    render() {
        return (
            <ResourceExplorerParent title={this.state.response.name + " Explorer"}
                                    content={this.renderMessageProcessorDetails()}
                                    breadcrumb={this.renderBreadCrumbs()}/>
        );
    }
}
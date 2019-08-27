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
import Typography from '@material-ui/core/Typography';

import Box from '@material-ui/core/Box';

export default class MessageProcessorDetailPage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            metaData: [],
            parameters: {}
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
                    parameters: parameters
                });

        }).catch((error) => {
            //Handle errors here
        });
    }

    renderMessageProcessorDetails() {
        return (
            <div>
                <Box pb={5}>
                    <Typography variant="h6" id="tableTitle">
                        Processor Details
                    </Typography>
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
                    <Typography variant="h6" id="tableTitle">
                        Parameters
                    </Typography>
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
            </div>
        );
    }

    render() {
        return (
            <ResourceExplorerParent content={this.renderMessageProcessorDetails()}/>
        );
    }
}
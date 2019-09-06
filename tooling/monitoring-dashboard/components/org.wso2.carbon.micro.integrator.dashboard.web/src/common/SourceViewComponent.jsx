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
import PropTypes from 'prop-types';
import {Prism as SyntaxHighlighter} from 'react-syntax-highlighter';
import {base16AteliersulphurpoolLight} from 'react-syntax-highlighter/dist/esm/styles/prism';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import TableHeaderBox from '../common/TableHeaderBox';

var format = require('xml-formatter');
import Box from '@material-ui/core/Box';


export default class SourceViewComponent extends Component {

    render() {
        var formatterConfiguration = format(this.props.config);
        return (

            <Box pb={5}>
                <TableHeaderBox title="Source View"/>
                <ExpansionPanel>
                    <ExpansionPanelSummary
                        expandIcon={<ExpandMoreIcon/>}
                        aria-controls="panel1a-content"
                        id="panel1a-header"/>
                    <ExpansionPanelDetails>
                        <Box width="100%">
                            <SyntaxHighlighter language={this.props.language} style={this.props.theme}
                                               showLineNumbers={true}
                                               wrapLines={true}>
                                {formatterConfiguration}
                            </SyntaxHighlighter>
                        </Box>
                    </ExpansionPanelDetails>
                </ExpansionPanel>
            </Box>


        );
    }
}

SourceViewComponent.propTypes = {
    config: PropTypes.string,
    theme: PropTypes.shape({}),
    language: PropTypes.string,
};

SourceViewComponent.defaultProps = {
    config: 'no config',
    theme: base16AteliersulphurpoolLight,
    language: 'xml',
};
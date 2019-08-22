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
import SyntaxHighlighter from 'react-syntax-highlighter';
import {docco} from 'react-syntax-highlighter/dist/esm/styles/hljs';
import PropTypes from 'prop-types';
var format = require('xml-formatter');
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';

export default class SourceViewParent extends Component {

    renderSourceViewContent() {
        var formatterConfiguration = format(this.props.config);
        return (
            <Paper>
                <Typography variant="h6" component="h3">
                    {this.props.title}
                </Typography>
            <SyntaxHighlighter language={this.props.language} style={docco} showLineNumbers={true} wrapLines={true}>
                {formatterConfiguration}
            </SyntaxHighlighter>
            </Paper>
        );
    }

    render() {
        return (<ListViewParent data={this.renderSourceViewContent()}/>);
    }
}

SourceViewParent.propTypes = {
    config: PropTypes.string,
    theme: PropTypes.shape({}),
    language: PropTypes.string,
    title: PropTypes.string,
};

SourceViewParent.defaultProps = {
    config: 'no config',
    theme: docco,
    language: 'xml',
    title: 'Source View',
};
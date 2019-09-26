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
import {docco} from 'react-syntax-highlighter/dist/esm/styles/hljs';
import PropTypes from 'prop-types';
import Typography from '@material-ui/core/Typography';
import Divider from '@material-ui/core/Divider';
import Box from '@material-ui/core/Box';

const styles = {
    contentDiv: {
        margin: '3%',
    },
    explorerHeaderDiv:{
        padding:'1%',
    },
    paper:{
        height:'100%'
    }
};

export default class ResourceExplorerParent extends Component {

    renderSourceViewContent() {
        return (
            <Box>
                <div id="exploreHeader" style={styles.explorerHeaderDiv}>
                    <Typography variant="h5" id="tableTitle">
                        {this.props.title}
                    </Typography>
                    <Box id={"breadcrumbs"} fontWeight="fontWeightLight">
                        {this.props.breadcrumb}
                    </Box>
                </div>

                <Divider/>

                <div id="content" style={styles.contentDiv}>
                    {this.props.content}
                </div>
            </Box>
        );
    }

    render() {
        return (<ListViewParent data={this.renderSourceViewContent()}/>);
    }
}

ResourceExplorerParent.propTypes = {
    config: PropTypes.string,
    theme: PropTypes.shape({}),
    language: PropTypes.string,
    title: PropTypes.string,
    content: PropTypes.element,
    breadcrumb: PropTypes.element
};

ResourceExplorerParent.defaultProps = {
    config: 'no config',
    theme: docco,
    language: 'xml',
    title: 'Explorer',
    content: '<span/>',
    breadcrumb: ' '

};
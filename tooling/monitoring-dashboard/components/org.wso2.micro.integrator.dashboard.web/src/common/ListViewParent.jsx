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

import defaultTheme from '../utils/Theme';
import Header from './Header';
import SideDrawer from './SideDrawer';
import {MuiThemeProvider} from 'material-ui/styles';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Box from '@material-ui/core/Box';
import Container from '@material-ui/core/Container';


/**
 * Style constants.
 */
const styles = {
    contentPaper: {
        height: "calc(100% - 50px)",
        width: 'calc(100% - 240px)',
        float: 'right',
        paddingTop: '40px',
        marginLeft: '240px',
        position: 'fixed',
        overflowY: 'auto'
    },
    footer: {
        padding: 5,
        position: "absolute",
        textAlign: "center",
        left: 0,
        bottom: 0,
        right: 0,
        backgroundColor: "#263238",
        height: '50px',
        marginTop: '25px',
        color: '#ffffff'
    },
};

export default class ListViewParent extends Component {

    /**
     * Render the general view of the synapse artifacts listing pages
     */
    renderParentView() {
        return (
            <MuiThemeProvider muiTheme={this.props.theme}>
                <Header title={this.props.title} theme={this.props.theme}/>
                <SideDrawer/>
                <Box flexGrow={1} id={"content-box"}>
                    <Box>
                        <Paper style={styles.contentPaper} id={"data-box"} square={true}>
                            {this.props.data}
                        </Paper>
                    </Box>
                    <Box id={"footer-box"}>
                        <Paper style={styles.footer} square={true}>
                            <Typography>
                                © 2005 - 2019 WSO2 Inc. All Rights Reserved.
                            </Typography>
                        </Paper>
                    </Box>
                </Box>
            </MuiThemeProvider>
        );
    }


    render() {
        return this.renderParentView();
    }
}

ListViewParent.propTypes = {
    title: PropTypes.string,
    theme: PropTypes.shape({}),
    data: PropTypes.element,

};

ListViewParent.defaultProps = {
    title: 'MICRO INTEGRATOR',
    data: <span/>,
    theme: defaultTheme,
};

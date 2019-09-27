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
import SourceViewParent from '../common/SourceViewParent';
import ResourceAPI from '../utils/apis/ResourceAPI';
import queryString from 'query-string'

export default class APISourceViewPage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            config: " ",
        };
    }

    /**
     * Retrieve api info from the MI.
     */
    componentDidMount() {
        let url = this.props.location.search;
        const values = queryString.parse(url) || {};
        this.retrieveApiInfo(values.name);
    }

    retrieveApiInfo(name) {
        new ResourceAPI().getApiByName(name).then((response) => {
            const config = response.data.configuration || '';
            this.setState({config: config});

        }).catch((error) => {
            //Handle errors here
        });
    }


    render() {
        return (
            <SourceViewParent config={this.state.config}/>
        );
    }
}
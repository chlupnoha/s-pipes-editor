import * as React from 'react';
import Messager from "../wrapper/Messager";
import I18nWrapper from "../../i18n/I18nWrapper";
import injectIntl from "../../utils/injectIntl";
import Ajax from "../../utils/Ajax";

import {Button, Modal} from "react-bootstrap";
import * as I18Store from "../../stores/I18nStore";
import Actions from "../../actions/Actions";

//TODO quite beefy component
class Validation extends React.Component {
    constructor() {
        super();
        this.state = {
            validationData: false,
            validationDataSelected: false,
            validationDataDetail: false,
            validationRules: false,
            validationRuleSelected: false,
            validationRuleDetail: false,
            modalVisible: false,
            modalMessage: ""
        };

        this.handleChangeData = this.handleChangeData.bind(this);
        this.handleChangeRule = this.handleChangeRule.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount() {
        Ajax.get('rest/validation/data').end(
            (data) => {
                console.log(data);
                this.setState({validationData : data});
            });
        Ajax.get('rest/validation/rules').end(
            (data) => {
                console.log(data);
                this.setState({validationRules : data});
            });
    }

    componentWillMount() {
        this.render();
    }

    handleChangeData(event) {
        if(event.target.value !== ""){
            Ajax.get('rest/validation/data/' + event.target.value).end(
                (d) => {
                    this.setState({validationDataDetail : d});
                });
        }
        this.setState({validationDataSelected : event.target.value});
    }

    handleChangeRule(event) {
        if(event.target.value !== ""){
            Ajax.get('rest/validation/rules/' + event.target.value).end(
                (d) => {
                    this.setState({validationRuleDetail : d});
                });
        }
        this.setState({validationRuleSelected : event.target.value});
    }

    handleSubmit(event) {
        if(!this.state.validationDataSelected|| !this.state.validationRuleSelected){
            this.setState({modalVisible: true, modalMessage: "Data and rule are required"});
        }else{
            var data = {data: this.state.validationDataSelected, rule: this.state.validationRuleSelected}
            Ajax.post('rest/validation/execute', data, "json").end(
                (d) => {
                    console.log("result is" + d);
                    this.setState({modalVisible: true, modalMessage: d})
                }
            )
            event.preventDefault();
        }

    }

    closeModal() {
        this.setState({modalVisible: false});
    };

    render() {
        if(!this.state.validationData || !this.state.validationRules){
            return <div>LOADING</div>
        }else{
            return (
                <div className="container">
                    <form onSubmit={this.handleSubmit}>
                        <h2>Validation form</h2>
                        <label>
                            Pick your data:
                            <select value={this.state.validationDataSelected} onChange={this.handleChangeData}>
                                <option key="" value="">{}</option>
                                {this.state.validationData.data.map((e) => {
                                    return <option key={e} value={e}>{e}</option>;
                                })}
                            </select>
                        </label>
                        <label>
                            Pick your rule:
                            <select value={this.state.validationRuleSelected} onChange={this.handleChangeRule}>
                                <option key="" value="">{}</option>
                                {this.state.validationRules.data.map((e) => {
                                    return <option key={e} value={e}>{e}</option>;
                                })}
                            </select>
                        </label>
                        <input type="submit" value="Submit" />
                        <div className="row" style={{whiteSpace: "pre-line"}}>
                            <h3>Add JS visualization</h3>
                            <div className="col-sm-6" >data detail: <br/>{this.state.validationDataDetail}</div>
                            <div className="col-sm-6" >rule detail: <br/>{this.state.validationRuleDetail}</div>
                        </div>
                    </form>
                    <Modal show={this.state.modalVisible}>
                        <Modal.Header>
                            <Modal.Title>Result</Modal.Title>
                        </Modal.Header>
                        <Modal.Body style={{whiteSpace: "pre-line"}}>
                            {this.state.modalMessage}
                        </Modal.Body>
                        <Modal.Footer>
                            <Button onClick={() => this.closeModal()}>OK</Button>
                        </Modal.Footer>
                    </Modal>
                </div>
            );
        }
    }

}

export default injectIntl(I18nWrapper(Messager(Validation)));
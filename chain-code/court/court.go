/*
SPDX-License-Identifier: Apache-2.0
*/

package main

import (
	"encoding/json"
	"fmt"
	"github.com/hyperledger/fabric-contract-api-go/contractapi"
	"time"
	"errors"
)

type SmartContract struct {
	contractapi.Contract
}

// Court 구조체
type Court struct {
	ID      					string   				 `json:"id"`
	Court         		string           `json:"court"`
	Support       		string           `json:"support"`
	Office        		string           `json:"office"`
	Owner             string           `json:"owner"`
	Members           []string         `json:"members"`
	Requests          []CourtRequest   `json:"requests"`
	FinalizedRequests []CourtRequest   `json:"finalized"`
}

// CourtRequest 구조체
type CourtRequest struct {
	ID            string          `json:"id"`
	DocumentID    string          `json:"documentId"`
	Action        string          `json:"action"`
	Payload       string          `json:"payload"`
	Finalized     bool            `json:"finalized"`
	RequestDate   string          `json:"requestDate"`
	FinalizeDate  string          `json:"finalizeDate,omitempty"`
	Status        string          `json:"status"`
	ErrorMessage  string          `json:"errorMessage,omitempty"`
	ForwardedTo   string          `json:"forwardedTo,omitempty"`
}

func (s *SmartContract) RegistryCourt(ctx contractapi.TransactionContextInterface, id, court, support, office, owner string) (*Court, error) {
	courts, err := s.GetAllCourts(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to get courts: %v", err)
	}

	for _, existingCourt := range courts {
		if existingCourt.Court == court && existingCourt.Support == support && existingCourt.Office == office {
			return nil, errors.New("a court with the same court, support, and office already exists")
		}
	}

	newCourt := &Court{
		ID:       id,
		Court:    court,
		Support:  support,
		Office:   office,
		Owner:    owner,
		Members:  []string{},
		Requests: []CourtRequest{},
		FinalizedRequests: []CourtRequest{},
	}

	courtJSON, err := json.Marshal(newCourt)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal court: %v", err)
	}

	err = ctx.GetStub().PutState(id, courtJSON)
	if err != nil {
		return nil, fmt.Errorf("failed to put state: %v", err)
	}

	return newCourt, nil
}

func (s *SmartContract) GetCourtByID(ctx contractapi.TransactionContextInterface, id string) (*Court, error) {
	courtJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}
	if courtJSON == nil {
		return nil, fmt.Errorf("court %s does not exist", id)
	}

	var court Court
	err = json.Unmarshal(courtJSON, &court)
	if err != nil {
		return nil, fmt.Errorf("failed to unmarshal court JSON: %v", err)
	}

	return &court, nil
}

func (s *SmartContract) GetAllCourts(ctx contractapi.TransactionContextInterface) ([]*Court, error) {
	resultsIterator, err := ctx.GetStub().GetStateByRange("", "")
	if err != nil {
		return nil, fmt.Errorf("failed to get state by range: %v", err)
	}
	defer resultsIterator.Close()

	var courts []*Court
	for resultsIterator.HasNext() {
		queryResponse, err := resultsIterator.Next()
		if err != nil {
			return nil, fmt.Errorf("failed to get next result: %v", err)
		}

		var court Court
		err = json.Unmarshal(queryResponse.Value, &court)
		if err != nil {
			return nil, fmt.Errorf("failed to unmarshal court JSON: %v", err)
		}

		courts = append(courts, &court)
	}

	return courts, nil
}

// 새로운 함수: 소유자 변경
func (s *SmartContract) ChangeOwner(ctx contractapi.TransactionContextInterface, courtID string, newOwner string) error {
	// court 가져오기
	court, err := s.GetCourtByID(ctx, courtID)
	if err != nil {
		return err
	}

	// 현재 호출자가 소유자인지 확인
	clientID, err := s.getClientID(ctx)
	if err != nil {
		return err
	}

	if court.Owner != clientID {
		return errors.New("only the current owner can change the owner")
	}

	// 새 소유자가 RegistryMSP에 속하는지 확인
	mspID, err := ctx.GetClientIdentity().GetMSPID()
	if err != nil {
		return err
	}

	if mspID != "RegistryMSP" {
		return errors.New("the new owner must belong to RegistryMSP")
	}

	// 소유자 변경
	court.Owner = newOwner

	// 상태 업데이트
	courtJSON, err := json.Marshal(court)
	if err != nil {
		return err
	}

	err = ctx.GetStub().PutState(courtID, courtJSON)
	if err != nil {
		return err
	}

	return nil
}

// 새로운 함수: 회원 추가
func (s *SmartContract) AddMember(ctx contractapi.TransactionContextInterface, courtID string, member string) error {
	// court 가져오기
	court, err := s.GetCourtByID(ctx, courtID)
	if err != nil {
		return err
	}

	// 현재 호출자가 소유자인지 확인
	clientID, err := s.getClientID(ctx)
	if err != nil {
		return err
	}

	if court.Owner != clientID {
		return errors.New("only the owner can add members")
	}

	// 추가할 멤버가 RegistryMSP에 속해 있는지 확인
	mspID, err := ctx.GetClientIdentity().GetMSPID()
	if err != nil {
		return err
	}

	if mspID != "RegistryMSP" {
		return errors.New("the member must belong to RegistryMSP")
	}

	// 멤버 추가
	court.Members = append(court.Members, member)

	// 상태 업데이트
	courtJSON, err := json.Marshal(court)
	if err != nil {
		return err
	}

	err = ctx.GetStub().PutState(courtID, courtJSON)
	if err != nil {
		return err
	}

	return nil
}

// 새로운 함수: 회원 삭제
func (s *SmartContract) RemoveMember(ctx contractapi.TransactionContextInterface, courtID string, member string) error {
	// court 가져오기
	court, err := s.GetCourtByID(ctx, courtID)
	if err != nil {
		return err
	}

	// 현재 호출자가 소유자인지 확인
	clientID, err := s.getClientID(ctx)
	if err != nil {
		return err
	}

	if court.Owner != clientID {
		return errors.New("only the owner can remove members")
	}

	// 멤버가 court의 members에 존재하는지 확인
	memberFound := false
	for i, m := range court.Members {
		if m == member {
			// 멤버 삭제
			court.Members = append(court.Members[:i], court.Members[i+1:]...)
			memberFound = true
			break
		}
	}

	if !memberFound {
		return fmt.Errorf("member %s not found in court %s", member, courtID)
	}

	// 상태 업데이트
	courtJSON, err := json.Marshal(court)
	if err != nil {
		return err
	}

	err = ctx.GetStub().PutState(courtID, courtJSON)
	if err != nil {
		return err
	}

	return nil
}

// 새로운 함수: unfinalized된 요청 가져오기
func (s *SmartContract) GetUnfinalizedRequests(ctx contractapi.TransactionContextInterface, courtID string) ([]CourtRequest, error) {
	court, err := s.GetCourtByID(ctx, courtID)
	if err != nil {
		return nil, err
	}

	var unfinalizedRequests []CourtRequest
	for _, request := range court.Requests {
		if !request.Finalized {
			unfinalizedRequests = append(unfinalizedRequests, request)
		}
	}

	return unfinalizedRequests, nil
}

func (s *SmartContract) AddRequest(ctx contractapi.TransactionContextInterface, courtID string, request CourtRequest) error {
	court, err := s.GetCourtByID(ctx, courtID)
	if err != nil {
		return err
	}

	clientID, err := s.getClientID(ctx)
	if err != nil {
		return err
	}

	if court.Owner != clientID && !contains(court.Members, clientID) {
		return errors.New("only the owner or members can add requests")
	}

	if request.ID == "" {
		return errors.New("request ID is required")
	}
	if request.DocumentID == "" {
		return errors.New("request DocumentID is required")
	}
	if request.Action == "" {
		return errors.New("request Action is required")
	}
	if request.Payload == "" {
		return errors.New("request Payload is required")
	}
	if request.RequestDate == "" {
		return errors.New("request RequestDate is required")
	}
	if request.Status == "" {
		request.Status = "Pending"
	}
	if request.FinalizeDate == "" {
		request.FinalizeDate = "-"
	}
	if request.ErrorMessage == "" {
		request.ErrorMessage = "-"
	}
	if request.ForwardedTo == "" {
		request.ForwardedTo = "-"
	}

	court.Requests = append(court.Requests, request)

	courtJSON, err := json.Marshal(court)
	if err != nil {
		return err
	}

	err = ctx.GetStub().PutState(courtID, courtJSON)
	if err != nil {
		return err
	}

	return nil
}

func contains(slice []string, item string) bool {
	for _, v := range slice {
		if v == item {
			return true
		}
	}
	return false
}

func (s *SmartContract) FinalizeRequest(ctx contractapi.TransactionContextInterface, courtID string, requestID string, status string, errorMessage string) error {
	// court 가져오기
	court, err := s.GetCourtByID(ctx, courtID)
	if err != nil {
		return err
	}

	// 현재 호출자가 소유자 또는 멤버인지 확인
	clientID, err := s.getClientID(ctx)
	if err != nil {
		return err
	}

	isAuthorized := court.Owner == clientID
	if !isAuthorized {
		for _, member := range court.Members {
			if member == clientID {
				isAuthorized = true
				break
			}
		}
	}

	if !isAuthorized {
		return errors.New("only the owner or members can finalize the request")
	}

	// 요청 찾기
	var request *CourtRequest
	for i := range court.Requests {
		if court.Requests[i].ID == requestID {
			request = &court.Requests[i]
			break
		}
	}

	if request == nil {
		return fmt.Errorf("request with ID %s does not exist in court %s", requestID, courtID)
	}

	if request.Finalized {
		return errors.New("request is already finalized")
	}

	var eventName string
	var eventPayload []byte

	if status == "success" {
		chaincodeName := "registry"
		
		// Payload 처리
		var payloadData map[string]interface{}
		if err := json.Unmarshal([]byte(request.Payload), &payloadData); err != nil {
			return fmt.Errorf("failed to unmarshal payload JSON: %v", err)
		}

		payloadBytes, err := json.Marshal(payloadData)
		if err != nil {
			return fmt.Errorf("failed to marshal payload data: %v", err)
		}

		actionArgs := [][]byte{[]byte(request.Action), []byte(request.DocumentID)}
		
		if len(request.Payload) > 0 {
			actionArgs = append(actionArgs, []byte(request.Payload))  // request.Payload는 string 타입
		}

		response := ctx.GetStub().InvokeChaincode(chaincodeName, actionArgs, "")
		if response.Status != 200 {
			return fmt.Errorf("failed to invoke action %s: %v", request.Action, response.Message)
		}

		request.Status = "Success"
		eventName = "RequestSucceeded"
		eventPayload, err = json.Marshal(request)
		if err != nil {
			return fmt.Errorf("failed to marshal event payload: %v", err)
		}
	} else {
		request.Status = "error"
		request.ErrorMessage = errorMessage

		eventName = "RequestFailed"
		eventPayload, err = json.Marshal(request)
		if err != nil {
			return fmt.Errorf("failed to marshal event payload: %v", err)
		}
	}

	request.Finalized = true
	request.FinalizeDate = time.Now().Format(time.RFC3339)
	court.FinalizedRequests = append(court.FinalizedRequests, *request)
	courtJSON, err := json.Marshal(court)
	if err != nil {
			return fmt.Errorf("failed to marshal court JSON: %v", err)
	}

	err = ctx.GetStub().PutState(court.ID, courtJSON)
	if err != nil {
			return fmt.Errorf("failed to put court: %v", err)
	}

	err = ctx.GetStub().SetEvent(eventName, eventPayload)
	if err != nil {
			return fmt.Errorf("failed to set event: %v", err)
	}

	return nil
}

func (s *SmartContract) getClientID(ctx contractapi.TransactionContextInterface) (string, error) {
	certBase64, err := ctx.GetClientIdentity().GetX509Certificate()
	if err != nil {
		return "", fmt.Errorf("failed to get client certificate: %v", err)
	}

	return certBase64.Subject.CommonName, nil
}

func (s *SmartContract) ForwardRequest(ctx contractapi.TransactionContextInterface, requestID string, targetCourtID string) error {
	requestJSON, err := ctx.GetStub().GetState(requestID)
	if err != nil {
		return fmt.Errorf("failed to get request: %v", err)
	}
	if requestJSON == nil {
		return errors.New("request does not exist")
	}

	var request CourtRequest
	if err := json.Unmarshal(requestJSON, &request); err != nil {
		return fmt.Errorf("failed to unmarshal request JSON: %v", err)
	}

	if request.Finalized {
		return errors.New("request is already finalized")
	}

	targetCourt, err := s.GetCourtByID(ctx, targetCourtID)
	if err != nil {
		return fmt.Errorf("failed to get target court: %v", err)
	}

	request.Finalized = true
	request.FinalizeDate = time.Now().Format(time.RFC3339)
	request.Status = "Forwarded"
	request.ForwardedTo = targetCourtID

	requestJSON, err = json.Marshal(request)
	if err != nil {
		return fmt.Errorf("failed to marshal court request: %v", err)
	}

	err = ctx.GetStub().PutState(requestID, requestJSON)
	if err != nil {
		return fmt.Errorf("failed to update court request: %v", err)
	}

	targetCourt.Requests = append(targetCourt.Requests, request)

	targetCourtJSON, err := json.Marshal(targetCourt)
	if err != nil {
		return fmt.Errorf("failed to marshal target court: %v", err)
	}

	err = ctx.GetStub().PutState(targetCourtID, targetCourtJSON)
	if err != nil {
		return fmt.Errorf("failed to update target court: %v", err)
	}

	return nil
}

func main() {
	chaincode, err := contractapi.NewChaincode(&SmartContract{})
	if err != nil {
		fmt.Printf("Error creating chaincode: %v\n", err)
		return
	}

	if err := chaincode.Start(); err != nil {
		fmt.Printf("Error starting chaincode: %v\n", err)
	}
}
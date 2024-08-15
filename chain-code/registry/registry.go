/*
SPDX-License-Identifier: Apache-2.0
*/

package main

import (
	"encoding/json"
	"fmt"
	"time"
	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

type SmartContract struct {
	contractapi.Contract
}

// 표제부 (Title Section)
type TitleSection struct {
	BuildingDescription      []BuildingDescription      `json:"buildingDescription"`      // 건물의 표시
	LandDescription          []LandDescription          `json:"landDescription"`          // 대지권의 목적인 토지의 표시
}

type BuildingDescription struct {
	DisplayNumber            string    `json:"displayNumber"`            // 표시번호
	ReceiptDate              time.Time `json:"receiptDate"`              // 접수
	LocationNumber           string    `json:"locationNumber"`           // 소재지번, 건물명칭 및 번호
	BuildingDetails          string    `json:"buildingDetails"`          // 건물내역
	RegistrationCause        string    `json:"registrationCause"`        // 등기원인 및 기타사항
	LocationNumberAlternate  string    `json:"locationNumberAlternate"`  // 소재지번 (Alternate field)
}

type LandDescription struct {
	DisplayNumber            string    `json:"displayNumber"`            // 표시번호
	LandType                 string    `json:"landType"`                 // 지목
	Area                     float64   `json:"area"`                     // 면적
	RegistrationCause        string    `json:"registrationCause"`        // 등기원인 및 기타사항
}

// 전유부분의 건물의 표시 (Exclusive Part of the Building)
type ExclusivePartDescription struct {
	BuildingDescription      []BuildingDescription      `json:"buildingDescription"`      // 전유부분의 건물의 표시
	LandRightDescription     []LandRightDescription     `json:"landRightDescription"`     // 대지권의 표시
}

type LandRightDescription struct {
	DisplayNumber            string    `json:"displayNumber"`            // 표시번호
	LandRightType            string    `json:"landRightType"`            // 대지권의 종류
	LandRightRatio           float64   `json:"landRightRatio"`           // 대지권 비율
	RegistrationCause        string    `json:"registrationCause"`        // 등기원인 및 기타사항
}

// 갑구 (First Section)
type FirstSection struct {
	RankNumber               int       `json:"rankNumber"`               // 순위번호
	RegistrationPurpose      string    `json:"registrationPurpose"`      // 등기목적
	ReceiptDate              time.Time `json:"receiptDate"`              // 접수
	RegistrationCause        string    `json:"registrationCause"`        // 등기원인
	HolderAndAdditionalInfo  string    `json:"holderAndAdditionalInfo"`  // 권리자 및 기타사항
}

// 을구 (Second Section)
type SecondSection struct {
	RankNumber               int       `json:"rankNumber"`               // 순위번호
	RegistrationPurpose      string    `json:"registrationPurpose"`      // 등기목적
	ReceiptDate              time.Time `json:"receiptDate"`              // 접수
	RegistrationCause        string    `json:"registrationCause"`        // 등기원인
	HolderAndAdditionalInfo  string    `json:"holderAndAdditionalInfo"`  // 권리자 및 기타사항
}

// 부동산 등기부등본 (Registry Document)
type RegistryDocument struct {
	ID                       string                     `json:"id"`                       // 등기부등본 ID
	TitleSection             []TitleSection             `json:"titleSection"`             // 표제부
	ExclusivePartDescription []ExclusivePartDescription `json:"exclusivePartDescription"` // 전유부분의 건물의 표시
	FirstSection             []FirstSection             `json:"firstSection"`             // 갑구
	SecondSection            []SecondSection            `json:"secondSection"`            // 을구
}

// fabric 초기화
func (s *SmartContract) InitLedger(ctx contractapi.TransactionContextInterface) error {
	return nil
}

// 등기부등본 생성
func (s *SmartContract) CreateRegistryDocument(ctx contractapi.TransactionContextInterface, document RegistryDocument) error {
	documentJSON, err := json.Marshal(document)
	if err != nil {
		return fmt.Errorf("failed to marshal document: %v", err)
	}

	return ctx.GetStub().PutState(document.ID, documentJSON)
}

// ReadRegistryDocument retrieves a RegistryDocument from the ledger by its ID
func (s *SmartContract) ReadRegistryDocument(ctx contractapi.TransactionContextInterface, id string) (*RegistryDocument, error) {
	documentJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return nil, fmt.Errorf("failed to read from world state: %v", err)
	}

	if documentJSON == nil {
		return nil, fmt.Errorf("the document %s does not exist", id)
	}

	var document RegistryDocument
	if err := json.Unmarshal(documentJSON, &document); err != nil {
		return nil, fmt.Errorf("failed to unmarshal document JSON: %v", err)
	}

	return &document, nil
}

func main() {
	chaincode, err := contractapi.NewChaincode(&SmartContract{})
	if err != nil {
		fmt.Printf("Error creating chaincode: %s", err.Error())
		return
	}

	if err := chaincode.Start(); err != nil {
		fmt.Printf("Error starting chaincode: %s", err.Error())
	}
}
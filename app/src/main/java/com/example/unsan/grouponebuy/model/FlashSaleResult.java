package com.example.unsan.grouponebuy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

public class FlashSaleResult implements Serializable {


        @JsonProperty("status")
        private Integer status;
        @JsonProperty("msg")
        private String msg;
        @JsonProperty("payload")
        private List<FlashSale> payload = null;

        @JsonProperty("status")
        public Integer getStatus() {
            return status;
        }

        @JsonProperty("status")
        public void setStatus(Integer status) {
            this.status = status;
        }

        @JsonProperty("msg")
        public String getMsg() {
            return msg;
        }

        @JsonProperty("msg")
        public void setMsg(String msg) {
            this.msg = msg;
        }

        @JsonProperty("payload")
        public List<FlashSale> getPayload() {
            return payload;
        }

        @JsonProperty("payload")
        public void setPayload(List<FlashSale> payload) {
            this.payload = payload;
        }
}

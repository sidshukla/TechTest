package com.db.dataplatform.techtest.server.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@JsonSerialize(as = DataBody.class)
@JsonDeserialize(as = DataBody.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class DataBody {

    @NotNull
    private String dataBody;

}

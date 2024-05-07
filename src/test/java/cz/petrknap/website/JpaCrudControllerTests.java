package cz.petrknap.website;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public abstract class JpaCrudControllerTests<T, ID> {
    @Autowired
    protected MockMvc mvc;

    @Test
    public void lists() throws Exception {
        mvc.perform(get(getRequestMapping() + "/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(getEntityId().toString())))
        ;
    }

    @Test
    public void creates() throws Exception {
        getRepository().deleteById(getEntityId());

        addExpects(
                mvc.perform(post(getRequestMapping() + "/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody(getCreateBodyAsKeyToRawValue()))
                        )
                        .andExpect(status().isCreated())
                        .andExpect(header().string(HttpHeaders.LOCATION, containsString(getEntityId().toString()))),
                getCreateBodyAsKeyToRawValue()
        );
    }

    @Test
    public void shows() throws Exception {
        addExpects(
                mvc.perform(get(getRequestMapping() + "/" + getEntityId()))
                        .andExpect(status().isOk()),
                getCreateBodyAsKeyToRawValue()
        );
    }

    @Test
    public void updates() throws Exception {
        addExpects(
            mvc.perform(put(getRequestMapping() + "/" + getEntityId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(getUpdateBodyAsKeyToRawValue()))
                )
                .andExpect(status().isOk()),
            getUpdateBodyAsKeyToRawValue()
        );
    }

    @Test
    public void deletes() throws Exception {
        mvc.perform(delete(getRequestMapping() + "/" + getEntityId()))
                .andExpect(status().isNoContent())
        ;

        assertThat(getRepository().findById(getEntityId())).isEmpty();
    }

    abstract protected JpaRepository<T, ID> getRepository();
    abstract protected String getRequestMapping();
    abstract protected ID getEntityId();
    abstract protected Map<String, String> getCreateBodyAsKeyToRawValue();
    abstract protected Map<String, String> getUpdateBodyAsKeyToRawValue();

    private String createBody(Map<String, String> keyToRawValue)
    {
        StringBuilder createBody = new StringBuilder("{");
        for (Map.Entry<String, String> entry : keyToRawValue.entrySet()) {
            createBody.append("\"").append(entry.getKey()).append("\":").append(entry.getValue()).append(",");
        }
        return createBody.deleteCharAt(createBody.length() - 1).append("}").toString();
    }

    private void addExpects(ResultActions resultActions, Map<String, String> keyToRawValue) throws Exception {
        for (Map.Entry<String, String> entry : keyToRawValue.entrySet()) {
            resultActions.andExpect(content().string(containsString(
                    "\"" + entry.getKey() + "\":" + entry.getValue()
            )));
        }
    }
}

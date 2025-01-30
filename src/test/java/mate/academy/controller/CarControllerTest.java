package mate.academy.controller;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import mate.academy.dto.PageResponse;
import mate.academy.dto.car.CarDto;
import mate.academy.model.Car;
import mate.academy.security.JwtUtil;
import mate.academy.service.car.CarServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarControllerTest {
    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private CarServiceImpl carService;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Sql(
            scripts = "classpath:"
                    + "database/04-delete-cars-users-rentals-for-rental-repository-test.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    @DisplayName("Create a new car with valid data")
    void createCar_ValidCarDto_ShouldReturnCarDto() throws Exception {
        CarDto carDto = new CarDto();
        carDto.setModel("X5");
        carDto.setBrand("BMW");
        carDto.setInventory(12);
        carDto.setType(Car.Type.SUV.toString());
        carDto.setDailyFee(BigDecimal.valueOf(120.99));

        CarDto expected = new CarDto();
        expected.setBrand(carDto.getBrand());
        expected.setModel(carDto.getModel());
        expected.setInventory(carDto.getInventory());
        expected.setType(carDto.getType());
        expected.setDailyFee(carDto.getDailyFee());

        String jsonRequest = objectMapper.writeValueAsString(carDto);

        MvcResult result = mockMvc.perform(post("/cars")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        CarDto actual = objectMapper
                .readValue(result.getResponse().getContentAsString(), CarDto.class);

        assertNotNull(actual);
        assertNotNull(actual.getId());
        assertTrue(reflectionEquals(expected, actual, "id"));
    }

    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Test
    @DisplayName("Create a new car with invalid data")
    void createCar_InvalidCarDto_ShouldThrowException() throws Exception {
        CarDto carDto = new CarDto();
        carDto.setType("WRONG");
        carDto.setModel("");
        carDto.setBrand("");
        carDto.setInventory(-3);
        carDto.setDailyFee(BigDecimal.valueOf(-50));

        String jsonRequest = objectMapper.writeValueAsString(carDto);

        mockMvc.perform(post("/cars")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andReturn();
    }

    @Sql(
            scripts = "classpath"
                    + ":database/03-create-cars-users-rentals-for-rental-repository-test.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:"
                    + "database/04-delete-cars-users-rentals-for-rental-repository-test.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @Test
    @DisplayName("Get all cars with valid pageable")
    void detAll_ValidPageable_ShouldReturnPageOfCars() throws Exception {
        CarDto carDto1 = new CarDto();
        carDto1.setId(1L);
        carDto1.setModel("X5");
        carDto1.setBrand("BMW");
        carDto1.setType("SUV");
        carDto1.setInventory(13);
        carDto1.setDailyFee(BigDecimal.valueOf(130.99));

        CarDto carDto2 = new CarDto();
        carDto2.setId(2L);
        carDto2.setModel("RS7");
        carDto2.setBrand("Audi");
        carDto2.setType("SEDAN");
        carDto2.setInventory(15);
        carDto2.setDailyFee(BigDecimal.valueOf(150.99));

        CarDto carDto3 = new CarDto();
        carDto3.setId(3L);
        carDto3.setModel("Laguna4");
        carDto3.setBrand("Renault");
        carDto3.setType("UNIVERSAL");
        carDto3.setInventory(10);
        carDto3.setDailyFee(BigDecimal.valueOf(110.99));

        List<CarDto> expected = new ArrayList();
        expected.add(carDto1);
        expected.add(carDto2);
        expected.add(carDto3);

        MvcResult result = mockMvc.perform(get("/cars")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        PageResponse<CarDto> actualPage = objectMapper
                .readValue(jsonResponse, new TypeReference<>() {});
        List<CarDto> actualList = actualPage.getContent();

        assertNotNull(actualPage);
        assertNotNull(actualPage.getContent());
        assertEquals(3, actualList.size());
        assertEquals(expected, actualList);
    }
}

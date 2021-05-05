package com.vaccinefinder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class VaccineSlotFinderApplication implements CommandLineRunner {

	RestTemplate restTemplate = new RestTemplate();

	@Value("#{'${pincodes}'.split(',')}")
	List<String> pincodes;

	@Value("${age}")
	private int age;
	
	@Value("${days}")
	private int days;


	DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

	public static void main(String[] args) {
		SpringApplication.run(VaccineSlotFinderApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		start();

	}

	public void start() {
		boolean found=false;
		LocalDateTime day = LocalDateTime.now();
		for (int i = 0; i < 7; i++) {
			LocalDateTime queryDay = day.plusDays(i);
			for (String pincode : pincodes) {
				JsonNode root = getDetails(dateformat.format(queryDay), pincode);
				JsonNode sessions = root.findValues("sessions").get(0);
				Iterator<JsonNode> arrayIterator = sessions.iterator();
				while (arrayIterator.hasNext()) {
					JsonNode center = arrayIterator.next();
					int ageLimit = center.get("min_age_limit").asInt();
					if (ageLimit <= age) {
						if(found==false) {
							System.out.println("Match Found !!");
							found=true;
						}
						System.out.println("Name : " + center.get("name").asText());
						System.out.println("Address : " + center.get("address").asText());
						System.out.println("Date : " + center.get("date").asText());
						System.out.println("Age limit : " + center.get("min_age_limit").asInt());
						System.out.println("Fee type : " + center.get("fee_type").asText());
						System.out.println("Available capacity : " + center.get("available_capacity").asText());
						System.out.println("Fee : " + center.get("fee").asInt());
						System.out.println("Vaccine : " + center.get("vaccine").asText());
						System.out.print("Slots : ");
						JsonNode slots = center.findValues("slots").get(0);
						
						Iterator<JsonNode> slotsIterator = slots.iterator();
						String tab="";
						while (slotsIterator.hasNext()) {
							JsonNode slot = slotsIterator.next();
							System.out.println(tab+slot.asText());
							tab="        ";
						}
						System.out.println();
					}
				}
				
			}
		}
		if(found==false) {
			System.out.println("No slots found for next "+days+" days");
		}
	}

	public JsonNode getDetails(String date, String pincode) {

		String fooResourceUrl = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/findByPin";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fooResourceUrl).queryParam("pincode", pincode)
				.queryParam("date", date);

		ResponseEntity<String> response = restTemplate.getForEntity(builder.toUriString(), String.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = null;
		String res = response.getBody();
		try {
			root = mapper.readTree(res);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return root;
	}

}


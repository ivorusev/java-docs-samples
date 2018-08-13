/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.dialogflow;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;


/**
 * Integration (system) tests for {@link EntityManagement} and {@link EntityTypeManagement}.
 */
@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class CreateDeleteEntityIT {
  private static String ENTITY_TYPE_DISPLAY_NAME = "fake_entity_type_for_testing";
  private static String ENTITY_VALUE_1 = "fake_entity_for_testing_1";
  private static String ENTITY_VALUE_2 = "fake_entity_for_testing_2";
  private static List<String> SYNONYMS = Arrays.asList("fake_synonym_for_testing_1",
      "fake_synonym_for_testing_2");

  private ByteArrayOutputStream bout;
  private PrintStream out;

  private EntityManagement entityManagement;
  private EntityTypeManagement entityTypeManagement;
  private static String PROJECT_ID = System.getenv().get("GOOGLE_CLOUD_PROJECT");

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
    entityManagement = new EntityManagement();
    entityTypeManagement = new EntityTypeManagement();
  }


  @After
  public void tearDown() {
    System.setOut(null);
  }

  @Test
  public void testCreateEntityType() throws Exception {
    List<String> entityTypeIds = entityTypeManagement.getEntityTypeIds(ENTITY_TYPE_DISPLAY_NAME,
        PROJECT_ID);

    assertThat(entityTypeIds.size()).isEqualTo(0);

    entityTypeManagement.createEntityType(ENTITY_TYPE_DISPLAY_NAME, PROJECT_ID, "KIND_MAP");

    String got = bout.toString();
    assertThat(got).contains(String.format("display_name: \"%s\"", ENTITY_TYPE_DISPLAY_NAME));

    entityTypeIds = entityTypeManagement.getEntityTypeIds(ENTITY_TYPE_DISPLAY_NAME,
        PROJECT_ID);

    assertThat(entityTypeIds.size()).isEqualTo(1);
  }

  @Test
  public void testCreateEntityWithCreatedEntityType() throws Exception {
    List<String> entityTypeIds = entityTypeManagement.getEntityTypeIds(ENTITY_TYPE_DISPLAY_NAME,
        PROJECT_ID);

    entityManagement.createEntity(PROJECT_ID, entityTypeIds.get(0), ENTITY_VALUE_1,
        Arrays.asList(""));
    entityManagement.createEntity(PROJECT_ID, entityTypeIds.get(0), ENTITY_VALUE_2, SYNONYMS);

    entityManagement.listEntities(PROJECT_ID, entityTypeIds.get(0));

    String got = bout.toString();
    assertThat(got).contains(String.format("Entity value: %s", ENTITY_VALUE_1));
    assertThat(got).contains(String.format("Entity value: %s", ENTITY_VALUE_2));

    for (String synonym : SYNONYMS) {
      assertThat(got).contains(synonym);
    }
  }

  @Test
  public void testDeleteEntity() throws Exception {
    List<String> entityTypeIds = entityTypeManagement.getEntityTypeIds(ENTITY_TYPE_DISPLAY_NAME,
        PROJECT_ID);

    entityManagement.deleteEntity(PROJECT_ID, entityTypeIds.get(0), ENTITY_VALUE_1);
    entityManagement.deleteEntity(PROJECT_ID, entityTypeIds.get(0), ENTITY_VALUE_2);

    entityManagement.listEntities(PROJECT_ID, entityTypeIds.get(0));

    String got = bout.toString();
    assertThat(got).isEqualTo("");
  }

  @Test
  public void testDeleteEntityType() throws Exception {
    List<String> entityTypeIds = entityTypeManagement.getEntityTypeIds(ENTITY_TYPE_DISPLAY_NAME,
        PROJECT_ID);

    for (String entityTypeId : entityTypeIds) {
      entityTypeManagement.deleteEntityType(entityTypeId, PROJECT_ID);
    }

    entityTypeIds = entityTypeManagement.getEntityTypeIds(ENTITY_TYPE_DISPLAY_NAME, PROJECT_ID);
    assertThat(entityTypeIds.size()).isEqualTo(0);
  }
}

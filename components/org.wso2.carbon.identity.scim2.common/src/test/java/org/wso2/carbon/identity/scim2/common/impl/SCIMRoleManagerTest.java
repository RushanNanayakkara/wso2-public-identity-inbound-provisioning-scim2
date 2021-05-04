/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.scim2.common.impl;

import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.role.mgt.core.GroupBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementServerException;
import org.wso2.carbon.identity.role.mgt.core.RoleBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.RoleConstants;
import org.wso2.carbon.identity.role.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.mgt.core.UserBasicInfo;
import org.wso2.carbon.identity.scim2.common.utils.SCIMCommonConstants;
import org.wso2.carbon.identity.scim2.common.utils.SCIMCommonUtils;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.charon3.core.exceptions.BadRequestException;
import org.wso2.charon3.core.exceptions.CharonException;
import org.wso2.charon3.core.exceptions.ConflictException;
import org.wso2.charon3.core.exceptions.NotFoundException;
import org.wso2.charon3.core.exceptions.NotImplementedException;
import org.wso2.charon3.core.objects.Group;
import org.wso2.charon3.core.objects.Role;
import org.wso2.charon3.core.objects.User;
import org.wso2.charon3.core.utils.codeutils.ExpressionNode;
import org.wso2.charon3.core.utils.codeutils.Node;
import org.wso2.charon3.core.utils.codeutils.OperationNode;
import org.wso2.charon3.core.utils.codeutils.SearchRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.INVALID_OFFSET;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.INVALID_LIMIT;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.INVALID_REQUEST;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.OPERATION_FORBIDDEN;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.ROLE_ALREADY_EXISTS;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.ROLE_NOT_FOUND;
import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.UNEXPECTED_SERVER_ERROR;

@PrepareForTest({SCIMCommonUtils.class})
public class SCIMRoleManagerTest extends PowerMockTestCase {

    private static final String SAMPLE_TENANT_DOMAIN = "carbon.super";
    private static final String SAMPLE_TENANT_DOMAIN2 = "abc.com";
    private static final String SAMPLE_INVALID_TENANT_DOMAIN = "invalid.org";
    private static final String SAMPLE_VALID_ROLE_ID = "8215b39a-49c6-4f91-9acf-4255fca362e5";
    private static final String SAMPLE_VALID_ROLE_ID2 = "1105d7a2-f91b-4f18-8b60-c53d02453372";
    private static final String SAMPLE_INVALID_ROLE_ID = "1614d770a5ba46afa3cb92d4cc097f3c";
    private static final String SAMPLE_INVALID_ROLE_ID2 = "cad7360942b0-4a2e-8ead-bf0c8@dbea602";
    private static final String SAMPLE_EXISTING_ROLE_ID = "6660279b-14ee-466b-895f-41f9a22ed5f1";
    private static final String SAMPLE_EXISTING_ROLE_ID2 = "73584b7d-45d9-4b0a-9b04-2ac7ff0d5a20";
    private static final String SAMPLE_NON_EXISTING_ROLE_ID = "04b5af38-0bf8-4fff-91c6-2425b273b17a";
    private static final String SAMPLE_NON_EXISTING_ROLE_ID2 = "ad26d674-b163-4cd6-a353-46322d60a491";
    private static final String SAMPLE_VALID_ROLE_NAME = "roleDisplayName1";
    private static final String SAMPLE_VALID_ROLE_NAME2 = "roleDisplayName2";
    private static final String SAMPLE_SYSTEM_ROLE_NAME = "roleDisplayName3";
    private static final String SAMPLE_SYSTEM_ROLE_NAME2 = "roleDisplayName4";
    private static final String SAMPLE_EXISTING_ROLE_NAME = "roleDisplayName5";
    private static final String SAMPLE_EXISTING_ROLE_NAME2 = "roleDisplayName6";
    private static final String SAMPLE_INVALID_ROLE_NAME = "system_roleName1";
    private static final String SAMPLE_INVALID_ROLE_NAME1 = "system_roleName2";
    private static final String SUCCESS = "success";
    private static final String FAIL = "fail";
    private static final String UNEXPECTED_ERROR = "unExpectedError";
    private static final String BAD_REQUEST = "badRequest";
    private static final String NOT_FOUND = "notFound";
    private static final String CONFLICT = "conflict";
    private static final String DUMMY_SCIM_URL =
            "https://localhost:9444/scim2/Roles/3891465e-4ecb-45f6-9822-e411c2deab64";
    private static final List<String> EXISTING_ROLE_IDS = Arrays.asList(SAMPLE_EXISTING_ROLE_ID,
            SAMPLE_EXISTING_ROLE_ID2);
    private static final List<String> INVALID_ROLE_IDS = Arrays.asList(SAMPLE_INVALID_ROLE_ID,
            SAMPLE_INVALID_ROLE_ID2);
    private static final List<String> INVALID_ROLE_NAMES = Arrays.asList(SAMPLE_INVALID_ROLE_NAME,
            SAMPLE_INVALID_ROLE_NAME1);
    private static final List<String> NON_EXISTING_ROLE_IDS = Arrays.asList(SAMPLE_NON_EXISTING_ROLE_ID,
            SAMPLE_NON_EXISTING_ROLE_ID2);
    private static final List<String> EXISTING_ROLE_NAMES = Arrays.asList(SAMPLE_EXISTING_ROLE_NAME,
            SAMPLE_EXISTING_ROLE_NAME2);
    private static final Set<String> SYSTEM_ROLES = new HashSet<>(Arrays.asList(SAMPLE_SYSTEM_ROLE_NAME,
            SAMPLE_SYSTEM_ROLE_NAME2));

    @Mock
    RoleManagementService mockRoleManagementService;

    @BeforeClass
    public void setUpClass() {

        initMocks(this);
    }

    @BeforeMethod
    public void setUpMethod() {

        mockStatic(SCIMCommonUtils.class);
        when(SCIMCommonUtils.getSCIMRoleURL(anyString())).thenReturn(DUMMY_SCIM_URL);
        when(mockRoleManagementService.getSystemRoles()).thenReturn(SYSTEM_ROLES);
    }

    @DataProvider(name = "dataProviderForCreateRoleExistingRole")
    public Object[][] dataProviderForCreateRoleExistingRole() {

        return new Object[][]{
                {SAMPLE_EXISTING_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_TENANT_DOMAIN, FAIL},
                {SAMPLE_EXISTING_ROLE_ID2, "", SAMPLE_TENANT_DOMAIN2, FAIL}
        };
    }

    @Test(dataProvider = "dataProviderForCreateRoleExistingRole")
    public void testCreateRoleExistingRole(String roleId, String roleDisplayName, String tenantDomain, String expect)
            throws IdentityRoleManagementException, BadRequestException, CharonException {

        Role role = getDummyRole(roleId, roleDisplayName);

        when(mockRoleManagementService.isExistingRole(anyString(), anyString()))
                .thenAnswer(invocationOnMock -> {
                    String roleIdArg = invocationOnMock.getArgumentAt(0, String.class);
                    return EXISTING_ROLE_IDS.contains(roleIdArg);
                });

        SCIMRoleManager scimRoleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            scimRoleManager.createRole(role);
            result = SUCCESS;
        } catch (ConflictException e) {
            result = FAIL;
        }
        assertEquals(expect, result);
    }

    @DataProvider(name = "dataProviderForCreateRoleAddRoleExistingRoleName")
    public Object[][] dataProviderForCreateRoleAddRoleExistingRoleName() {

        return new Object[][]{
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_EXISTING_ROLE_NAME, SAMPLE_TENANT_DOMAIN, FAIL},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_TENANT_DOMAIN, SUCCESS}
        };
    }

    @Test(dataProvider = "dataProviderForCreateRoleAddRoleExistingRoleName")
    public void testCreateRoleAddRoleExistingRoleName(String roleId, String roleDisplayName, String tenantDomain,
                                                      String expect)
            throws BadRequestException, CharonException, IdentityRoleManagementException {

        Role role = getDummyRole(roleId, roleDisplayName);

        when(mockRoleManagementService.addRole(anyString(), anyListOf(String.class), anyListOf(String.class),
                anyListOf(String.class), anyString())).
                thenAnswer(invocationOnMock -> {
                    String roleNameArg = invocationOnMock.getArgumentAt(0, String.class);
                    if (EXISTING_ROLE_NAMES.contains(roleNameArg)) {
                        throw new IdentityRoleManagementException(ROLE_ALREADY_EXISTS.getCode(),
                                "Role already exist for the role name: " + roleNameArg);
                    }
                    return new RoleBasicInfo(roleId, roleDisplayName);
                });

        SCIMRoleManager scimRoleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            Role scimRole = scimRoleManager.createRole(role);
            assertEquals(scimRole.getDisplayName(), roleDisplayName);
            assertEquals(scimRole.getId(), roleId);
            assertEquals(scimRole.getLocation(), DUMMY_SCIM_URL);
            result = SUCCESS;
        } catch (ConflictException e) {
            result = FAIL;
        }
        assertEquals(expect, result);
    }

    @DataProvider(name = "dataProviderForCreateRoleAddRoleInvalidRoleName")
    public Object[][] dataProviderForCreateRoleAddRoleInvalidRoleName() {

        return new Object[][]{
                {SAMPLE_VALID_ROLE_ID, SAMPLE_INVALID_ROLE_NAME, SAMPLE_TENANT_DOMAIN, FAIL},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN2, SUCCESS}
        };
    }

    @Test(dataProvider = "dataProviderForCreateRoleAddRoleInvalidRoleName")
    public void testCreateRoleAddRoleInvalidRoleName(String roleId, String roleDisplayName, String tenantDomain,
                                                     String expect)
            throws BadRequestException, CharonException, ConflictException, IdentityRoleManagementException {

        Role role = getDummyRole(roleId, roleDisplayName);

        when(mockRoleManagementService.addRole(anyString(), anyListOf(String.class), anyListOf(String.class),
                anyListOf(String.class), anyString())).
                thenAnswer(invocationOnMock -> {
                    String roleNameArg = invocationOnMock.getArgumentAt(0, String.class);
                    if (INVALID_ROLE_NAMES.contains(roleNameArg)) {
                        throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(),
                                String.format("Invalid role name: %s. Role names with the prefix: %s, is not allowed"
                                                + " to be created from externally in the system.", roleNameArg,
                                        UserCoreConstants.INTERNAL_SYSTEM_ROLE_PREFIX));
                    }
                    return new RoleBasicInfo(roleId, roleDisplayName);
                });

        SCIMRoleManager scimRoleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            Role scimRole = scimRoleManager.createRole(role);
            assertEquals(scimRole.getDisplayName(), roleDisplayName);
            assertEquals(scimRole.getId(), roleId);
            assertEquals(scimRole.getLocation(), DUMMY_SCIM_URL);
            result = SUCCESS;
        } catch (BadRequestException e) {
            result = FAIL;
        }
        assertEquals(expect, result);
    }

    @DataProvider(name = "dataProviderForCreateRoleUnexpectedServerError")
    public Object[][] dataProviderForCreateRoleUnexpectedServerError() {

        return new Object[][]{
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN, "sql error", FAIL},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_TENANT_DOMAIN2, null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, "", SAMPLE_INVALID_TENANT_DOMAIN, null, FAIL}
        };
    }

    @Test(dataProvider = "dataProviderForCreateRoleUnexpectedServerError")
    public void testCreateRoleUnexpectedServerError(String roleId, String roleDisplayName, String tenantDomain,
                                                    String sError, String expect)
            throws BadRequestException, CharonException, ConflictException, IdentityRoleManagementException {

        Role role = getDummyRole(roleId, roleDisplayName);

        when(mockRoleManagementService.addRole(anyString(), anyListOf(String.class), anyListOf(String.class),
                anyListOf(String.class), anyString())).
                thenAnswer(invocationOnMock -> {
                    String roleNameArg = invocationOnMock.getArgumentAt(0, String.class);
                    String tenantDomainArg = invocationOnMock.getArgumentAt(4, String.class);
                    unExpectedErrorThrower(tenantDomainArg, sError,
                            "Error while creating the role: %s in the tenantDomain: %s", roleNameArg);
                    return new RoleBasicInfo(roleId, roleDisplayName);
                });

        SCIMRoleManager scimRoleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            Role scimRole = scimRoleManager.createRole(role);
            assertEquals(scimRole.getDisplayName(), roleDisplayName);
            assertEquals(scimRole.getId(), roleId);
            assertEquals(scimRole.getLocation(), DUMMY_SCIM_URL);
            result = SUCCESS;
        } catch (CharonException e) {
            result = FAIL;
        }
        assertEquals(expect, result);
    }

    @DataProvider(name = "dataProviderForCreateRolePositive")
    public Object[][] dataProviderForCreateRolePositive() {

        return new Object[][]{
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_TENANT_DOMAIN, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, "", SAMPLE_TENANT_DOMAIN, SUCCESS},
                {SAMPLE_VALID_ROLE_ID, null, SAMPLE_TENANT_DOMAIN, SUCCESS},
                {"", null, SAMPLE_TENANT_DOMAIN, SUCCESS},
                {null, null, SAMPLE_TENANT_DOMAIN, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, null, "", SUCCESS},
                {"", "", SAMPLE_TENANT_DOMAIN, SUCCESS},
        };
    }

    @Test(dataProvider = "dataProviderForCreateRolePositive")
    public void testCreateRolePositive(String roleId, String roleDisplayName, String tenantDomain, String expect)
            throws IdentityRoleManagementException, BadRequestException, CharonException, ConflictException {

        Role role = getDummyRole(roleId, roleDisplayName);

        when(mockRoleManagementService.addRole(anyString(), anyListOf(String.class), anyListOf(String.class),
                anyListOf(String.class), anyString())).
                thenReturn(new RoleBasicInfo(roleId, roleDisplayName));

        SCIMRoleManager scimRoleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);

        Role createdRole = scimRoleManager.createRole(role);
        assertEquals(createdRole.getDisplayName(), roleDisplayName);
        assertEquals(createdRole.getId(), roleId);
        assertEquals(expect, SUCCESS);
    }

    @DataProvider(name = "dataProviderForGetRoleNotFound")
    public Object[][] dataProviderForGetRoleNotFound() {

        return new Object[][]{
                {SAMPLE_NON_EXISTING_ROLE_ID, SAMPLE_VALID_ROLE_NAME, "roleDomain1", SAMPLE_TENANT_DOMAIN,
                        "urn:ietf:params:scim:schemas:extension:2.0:Role:groups.value", true, FAIL},
                {SAMPLE_NON_EXISTING_ROLE_ID2, SAMPLE_VALID_ROLE_NAME2, "roleDomainX", SAMPLE_INVALID_TENANT_DOMAIN,
                        "urn:ietf:params:scim:schemas:core:2.0:id", false, FAIL}
        };
    }

    @Test(dataProvider = "dataProviderForGetRoleNotFound")
    public void testGetRoleNotFound(String roleId, String roleName, String domain, String tenantDomain,
                                    String attributeKey, boolean attributeValue, String expected)
            throws IdentityRoleManagementException, BadRequestException, CharonException {

        org.wso2.carbon.identity.role.mgt.core.Role role = getDummyIdentityRole(roleId, roleName, domain, tenantDomain);
        Map<String, Boolean> attributeMap = null;
        if (attributeKey != null) {
            attributeMap = new HashMap<>();
            attributeMap.put(attributeKey, attributeValue); //dummy details
        }

        when(mockRoleManagementService.getRole(roleId, tenantDomain)).
                thenAnswer(invocationOnMock -> {
                    String roleIdArg = invocationOnMock.getArgumentAt(0, String.class);
                    String tenantDomainArg = invocationOnMock.getArgumentAt(1, String.class);
                    if (NON_EXISTING_ROLE_IDS.contains(roleIdArg)) {
                        String errorMessage = "A role doesn't exist with id: " + roleIdArg +
                                " in the tenantDomain: " + tenantDomainArg;
                        throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(), errorMessage);
                    }
                    return role;
                });

        SCIMRoleManager scimRoleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            Role scimRole = scimRoleManager.getRole(roleId, attributeMap);
            assertScimRoleFull(scimRole, roleId);
            result = SUCCESS;
        } catch (NotFoundException e) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForGetRoleUnexpectedServerError")
    public Object[][] dataProviderForGetRoleUnexpectedServerError() {

        return new Object[][]{
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, "roleDomain1", SAMPLE_TENANT_DOMAIN,
                        "urn:ietf:params:scim:schemas:extension:2.0:Role:groups.value", true, null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_VALID_ROLE_NAME2, null, SAMPLE_TENANT_DOMAIN2,
                        "urn:ietf:params:scim:schemas:extension:2.0:Role:groups.value", false, "sql error", FAIL},
                {SAMPLE_VALID_ROLE_ID, null, "roleDomain1", SAMPLE_INVALID_TENANT_DOMAIN, null, false, null, FAIL},
                {SAMPLE_VALID_ROLE_ID2, "", "roleDomainX", SAMPLE_INVALID_TENANT_DOMAIN, "", true, "sql error", FAIL},
                {"", "", "", "", "", true, null, SUCCESS}
        };
    }

    @Test(dataProvider = "dataProviderForGetRoleUnexpectedServerError")
    public void testGetRoleUnexpectedServerError(String roleId, String roleName, String domain, String tenantDomain,
                                                 String attributeKey, Boolean attributeValue, String sError,
                                                 String expected)
            throws IdentityRoleManagementException, BadRequestException, NotFoundException {

        org.wso2.carbon.identity.role.mgt.core.Role role = getDummyIdentityRole(roleId, roleName, domain, tenantDomain);
        Map<String, Boolean> attributeMap = null;
        if (attributeKey != null) {
            attributeMap = new HashMap<>();
            attributeMap.put(attributeKey, attributeValue); //dummy details
        }

        when(mockRoleManagementService.getRole(roleId, tenantDomain)).
                thenAnswer(invocationOnMock -> {
                    String roleIdArg = invocationOnMock.getArgumentAt(0, String.class);
                    String tenantDomainArg = invocationOnMock.getArgumentAt(1, String.class);
                    unExpectedErrorThrower(tenantDomainArg, sError,
                            "Error while creating the role: %s in the tenantDomain: %s", roleIdArg);
                    return role;
                });

        SCIMRoleManager scimRoleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            Role scimRole = scimRoleManager.getRole(roleId, attributeMap);
            assertEquals(scimRole.getId(), roleId);
            assertEquals(scimRole.getUsers().get(0), "7646b885-4207-4ca0-bc65-5df82272b6d1");
            assertEquals(scimRole.getPermissions().get(0), "permission");
            assertEquals(scimRole.getGroups().get(0), "26d3a726-9c00-4f4c-8a4e-f5e310138081");
            assertEquals(scimRole.getLocation(), DUMMY_SCIM_URL);
            result = SUCCESS;
        } catch (CharonException e) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForGetRolePositive")
    public Object[][] dataProviderForGetRolePositive() {

        return new Object[][]{
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, "roleDomain1", SAMPLE_TENANT_DOMAIN,
                        "urn:ietf:params:scim:schemas:extension:2.0:Role:groups.value", true, false, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_VALID_ROLE_NAME2, null, SAMPLE_TENANT_DOMAIN,
                        "urn:ietf:params:scim:schemas:extension:2.0:Role:groups.value", false, false, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_SYSTEM_ROLE_NAME, null, SAMPLE_TENANT_DOMAIN,
                        "urn:ietf:params:scim:schemas:extension:2.0:Role:groups.value", false, true, SUCCESS},
                {SAMPLE_VALID_ROLE_ID, null, "roleDomain1", SAMPLE_TENANT_DOMAIN2, null, false, true, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, "", "roleDomainX", SAMPLE_TENANT_DOMAIN, "", true, false, SUCCESS},
                {null, SAMPLE_VALID_ROLE_NAME, "", SAMPLE_TENANT_DOMAIN2, null, true, false, SUCCESS},
                {"", "", "", "", "", false, true, SUCCESS}
        };
    }

    @Test(dataProvider = "dataProviderForGetRolePositive")
    public void testGetRolePositive(String roleId, String roleName, String domain, String tenantDomain,
                                    String attributeKey, Boolean attributeValue, boolean isEmptyLists,
                                    String expected)
            throws IdentityRoleManagementException, BadRequestException, NotFoundException {

        org.wso2.carbon.identity.role.mgt.core.Role role = getDummyIdentityRole(roleId, roleName, domain, tenantDomain,
                isEmptyLists);
        Map<String, Boolean> attributeMap = null;
        if (attributeKey != null) {
            attributeMap = new HashMap<>();
            attributeMap.put(attributeKey, attributeValue); //dummy details
        }
        when(mockRoleManagementService.getRole(roleId, tenantDomain)).
                thenReturn(role);

        SCIMRoleManager scimRoleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            Role scimRole = scimRoleManager.getRole(roleId, attributeMap);
            assertScimRoleFull(scimRole, roleId);
            result = SUCCESS;
        } catch (CharonException e) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForDeleteRoleNonExistingRoleId")
    public Object[][] dataProviderForDeleteRoleNonExistingRoleId() {

        return new Object[][]{
                {SAMPLE_NON_EXISTING_ROLE_ID2, SAMPLE_TENANT_DOMAIN, FAIL},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_TENANT_DOMAIN2, SUCCESS}
        };
    }

    @Test(dataProvider = "dataProviderForDeleteRoleNonExistingRoleId")
    public void testDeleteRoleNonExistingRoleId(String roleId, String tenantDomain, String expected)
            throws IdentityRoleManagementException, CharonException, BadRequestException {

        doAnswer(invocationOnMock -> {
            String roleIdArg = invocationOnMock.getArgumentAt(0, String.class);
            String tenantDomainArg = invocationOnMock.getArgumentAt(1, String.class);
            if (NON_EXISTING_ROLE_IDS.contains(roleIdArg)) {
                String errorMessage = "A role doesn't exist with id: " + roleIdArg +
                        " in the tenantDomain: " + tenantDomainArg;
                throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(), errorMessage);
            }
            return null;
        }).doNothing().when(mockRoleManagementService).deleteRole(roleId, tenantDomain);
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            roleManager.deleteRole(roleId);
            verify(mockRoleManagementService, times(1)).deleteRole(roleId, tenantDomain);
            result = SUCCESS;
        } catch (NotFoundException e) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForDeleteRoleUnDeletableRole")
    public Object[][] dataProviderForDeleteRoleUnDeletableRole() {

        return new Object[][]{
                {"adminId", SAMPLE_TENANT_DOMAIN, FAIL},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_TENANT_DOMAIN, SUCCESS}
        };
    }

    @Test(dataProvider = "dataProviderForDeleteRoleUnDeletableRole")
    public void testDeleteRoleUnDeletableRole(String roleId, String tenantDomain, String expected)
            throws IdentityRoleManagementException, NotFoundException, CharonException {

        doAnswer(invocationOnMock -> {
            String roleIdArg = invocationOnMock.getArgumentAt(0, String.class);
            if (roleIdArg.equals("adminId")) {
                throw new IdentityRoleManagementClientException(OPERATION_FORBIDDEN.getCode(),
                        "Invalid operation. Role: " + roleIdArg + " Cannot be deleted.");
            }
            return null;
        }).doNothing().when(mockRoleManagementService).deleteRole(roleId, tenantDomain);

        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            roleManager.deleteRole(roleId);
            verify(mockRoleManagementService, times(1)).deleteRole(roleId, tenantDomain);
            result = SUCCESS;
        } catch (BadRequestException e) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForDeleteRoleUnExpectedError")
    public Object[][] dataProviderForDeleteRoleUnExpectedError() {

        return new Object[][]{
                {SAMPLE_VALID_ROLE_ID, SAMPLE_INVALID_TENANT_DOMAIN, "sql error", FAIL},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_TENANT_DOMAIN, null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_INVALID_TENANT_DOMAIN, null, FAIL},
                {"", SAMPLE_TENANT_DOMAIN2, "sql error", FAIL},
        };
    }

    @Test(dataProvider = "dataProviderForDeleteRoleUnExpectedError")
    public void testDeleteRoleUnExpectedError(String roleId, String tenantDomain, String sError, String expected)
            throws IdentityRoleManagementException, NotFoundException, BadRequestException {

        doAnswer(invocationOnMock -> {
            String roleIdArg = invocationOnMock.getArgumentAt(0, String.class);
            String tenantDomainArg = invocationOnMock.getArgumentAt(1, String.class);
            unExpectedErrorThrower(tenantDomainArg, sError,
                    "Error while creating the role: %s in the tenantDomain: %s", roleIdArg);
            return null;
        }).doNothing().when(mockRoleManagementService).deleteRole(roleId, tenantDomain);

        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            roleManager.deleteRole(roleId);
            verify(mockRoleManagementService, times(1)).deleteRole(roleId, tenantDomain);
            result = SUCCESS;
        } catch (CharonException e) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForDeleteRolePositive")
    public Object[][] dataProviderForDeleteRolePositive() {

        return new Object[][]{
                {SAMPLE_VALID_ROLE_ID, SAMPLE_TENANT_DOMAIN, SUCCESS},
                {"", SAMPLE_TENANT_DOMAIN2, SUCCESS},
                {null, SAMPLE_TENANT_DOMAIN, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, null, SUCCESS}
        };
    }

    @Test(dataProvider = "dataProviderForDeleteRolePositive")
    public void testDeleteRolePositive(String roleId, String tenantDomain, String expected)
            throws IdentityRoleManagementException {

        doNothing().when(mockRoleManagementService).deleteRole(roleId, tenantDomain);
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);

        String result;
        try {
            roleManager.deleteRole(roleId);
            verify(mockRoleManagementService, times(1)).deleteRole(roleId, tenantDomain);
            result = SUCCESS;
        } catch (Exception e) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithGETSortingNotSupport")
    public Object[][] dataProviderForListRolesWithGETSortingNotSupport() {

        return new Object[][]{
                {1, 3, "name", "ascending", SAMPLE_TENANT_DOMAIN, FAIL},
                {2, 2, null, "ascending", SAMPLE_TENANT_DOMAIN2, FAIL},
                {2, 5, "", "ascending", SAMPLE_TENANT_DOMAIN, FAIL},
                {0, 0, "name", null, SAMPLE_TENANT_DOMAIN2, FAIL},
                {3, 0, "name", "", SAMPLE_TENANT_DOMAIN, FAIL},
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithGETSortingNotSupport")
    public void testListRolesWithGETSortingNotSupport(Integer startIndex, Integer count, String sortBy,
                                                      String sortOrder, String tenantDomain, String expected)
            throws BadRequestException, CharonException {

        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            roleManager.listRolesWithGET(null, startIndex, count, sortBy, sortOrder);
            result = SUCCESS;
        } catch (NotImplementedException e) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithGETCountNullZero")
    public Object[][] dataProviderForListRolesWithGETCountNullZero() {

        return new Object[][]{
                {1, 0, null, null, SAMPLE_TENANT_DOMAIN2, SUCCESS},
                {2, 0, null, null, SAMPLE_TENANT_DOMAIN, SUCCESS},
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithGETCountNullZero")
    public void testListRolesWithGETCountNullZero(Integer startIndex, Integer count, String sortBy, String sortOrder,
                                                  String tenantDomain, String expected) {

        String result;
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        List<Object> roles;
        try {
            roles = roleManager.listRolesWithGET(null, startIndex, count, sortBy, sortOrder);
            assertEquals(roles.size(), 0);
            result = SUCCESS;
        } catch (Exception e) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithGETInvalidLimit")
    public Object[][] dataProviderForListRolesWithGETInvalidLimit() {

        return new Object[][]{
                {"Expression", 1, -2, null, null, SAMPLE_TENANT_DOMAIN2, "value", FAIL},
                {null, 2, -5, null, null, SAMPLE_TENANT_DOMAIN, "attributes", FAIL},
                {null, 2, 6, null, null, SAMPLE_TENANT_DOMAIN2, null, SUCCESS},
                {"Expression", 4, 6, null, null, SAMPLE_TENANT_DOMAIN, "attributes", SUCCESS},
                {null, 2, null, null, null, SAMPLE_TENANT_DOMAIN2, null, SUCCESS},
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithGETInvalidLimit")
    public void testListRolesWithGETInvalidLimit(String nodeType, Integer startIndex, Integer count, String sortBy,
                                                 String sortOrder, String tenantDomain, String attributes,
                                                 String expected)
            throws BadRequestException, IdentityRoleManagementException, NotImplementedException {

        Node rootNode = generateNodeBasedOnNodeType(nodeType, attributes);
        String searchFilter;
        searchFilter = attributes;
        List<RoleBasicInfo> roleList = getDummyRoleBasicInfoList();

        when(mockRoleManagementService.getRoles(count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> {
                    Integer countArg = invocationOnMock.getArgumentAt(0, Integer.class);
                    if (countArg != null && countArg < 0) {
                        String errorMessage =
                                "Invalid limit requested. Limit value should be greater than or equal to zero. limit: "
                                        + count;
                        throw new IdentityRoleManagementClientException(INVALID_LIMIT.getCode(), errorMessage);
                    }
                    return roleList;
                });
        when(mockRoleManagementService.getRoles(searchFilter, count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> {
                    Integer countArg = invocationOnMock.getArgumentAt(1, Integer.class);
                    if (countArg != null && countArg < 0) {
                        String errorMessage =
                                "Invalid limit requested. Limit value should be greater than or equal to zero. limit: "
                                        + count;
                        throw new IdentityRoleManagementClientException(INVALID_LIMIT.getCode(), errorMessage);
                    }
                    return roleList;
                });

        String result;
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        List<Object> roles;
        try {
            roles = roleManager.listRolesWithGET(rootNode, startIndex, count, sortBy, sortOrder);
            assertEquals(((Role) roles.get(1)).getDisplayName(), "roleDisplayName1");
            assertEquals(((Role) roles.get(1)).getId(), "role1");
            result = SUCCESS;
        } catch (CharonException notImplementedException) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithGETInvalidOffset")
    public Object[][] dataProviderForListRolesWithGETInvalidOffset() {

        return new Object[][]{
                {"Expression", -1, 2, null, null, SAMPLE_TENANT_DOMAIN2, "value", FAIL},
                {null, -2, 4, null, null, SAMPLE_TENANT_DOMAIN2, "attributes", FAIL},
                {null, 2, 6, null, null, SAMPLE_TENANT_DOMAIN2, null, SUCCESS},
                {"Expression", 4, 2, null, null, SAMPLE_TENANT_DOMAIN, "attributes", SUCCESS},
                {"Expression", null, 2, null, null, SAMPLE_TENANT_DOMAIN, "attributes", SUCCESS},
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithGETInvalidOffset")
    public void testListRolesWithGETInvalidOffset(String nodeType, Integer startIndex, Integer count,
                                                  String sortBy, String sortOrder, String tenantDomain,
                                                  String attributes, String expected)
            throws BadRequestException, IdentityRoleManagementException, NotImplementedException {

        Node rootNode = generateNodeBasedOnNodeType(nodeType, attributes);
        String searchFilter;
        searchFilter = attributes;
        List<RoleBasicInfo> roleList = getDummyRoleBasicInfoList();

        when(mockRoleManagementService.getRoles(count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> {
                    Integer startIndexArg = invocationOnMock.getArgumentAt(1, Integer.class);

                    if (startIndexArg != null && startIndexArg < 0) {
                        String errorMessage =
                                "Invalid offset requested. Offset value should be zero or greater than zero. offSet: "
                                        + startIndexArg;
                        throw new IdentityRoleManagementClientException(INVALID_OFFSET.getCode(), errorMessage);
                    }
                    return roleList;
                });
        when(mockRoleManagementService.getRoles(searchFilter, count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> {
                    Integer startIndexArg = invocationOnMock.getArgumentAt(2, Integer.class);

                    if (startIndexArg != null && startIndexArg < 0) {
                        String errorMessage =
                                "Invalid offset requested. Offset value should be zero or greater than zero. offSet: "
                                        + startIndexArg;
                        throw new IdentityRoleManagementClientException(INVALID_OFFSET.getCode(), errorMessage);
                    }
                    return roleList;
                });
        String result;
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        List<Object> roles;
        try {
            roles = roleManager.listRolesWithGET(rootNode, startIndex, count, sortBy, sortOrder);
            assertEquals(((Role) roles.get(1)).getDisplayName(), "roleDisplayName1");
            assertEquals(((Role) roles.get(1)).getId(), "role1");
            result = SUCCESS;
        } catch (CharonException charonException) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithGETUnExpectedServerError")
    public Object[][] dataProviderForListRolesWithGETUnExpectedServerError() {

        return new Object[][]{
                {"Expression", 3, 2, null, null, SAMPLE_INVALID_TENANT_DOMAIN, "value", null, FAIL},
                {null, 2, 6, null, null, SAMPLE_TENANT_DOMAIN, null, null, SUCCESS},
                {"Expression", 4, 2, null, null, SAMPLE_TENANT_DOMAIN2, "attributes", null, SUCCESS},
                {null, 6, 4, null, null, SAMPLE_TENANT_DOMAIN2, "attributes", "sql error", FAIL},
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithGETUnExpectedServerError")
    public void testListRolesWithGETUnExpectedServerError(String nodeType, Integer startIndex, Integer count,
                                                          String sortBy, String sortOrder, String tenantDomain,
                                                          String attributes, String sError, String expected)
            throws BadRequestException, IdentityRoleManagementException, NotImplementedException {

        Node rootNode = generateNodeBasedOnNodeType(nodeType, attributes);
        String searchFilter;
        searchFilter = attributes;
        List<RoleBasicInfo> roleList = getDummyRoleBasicInfoList();

        when(mockRoleManagementService.getRoles(count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> {
                    String tenantDomainArg = invocationOnMock.getArgumentAt(4, String.class);
                    unExpectedErrorThrower(tenantDomainArg, sError,
                            "Error while listing roles in tenantDomain: ");
                    return roleList;
                });
        when(mockRoleManagementService.getRoles(searchFilter, count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> {
                    String tenantDomainArg = invocationOnMock.getArgumentAt(5, String.class);
                    unExpectedErrorThrower(tenantDomainArg, sError,
                            "Error while listing roles in tenantDomain: ");
                    return roleList;
                });

        String result;
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        List<Object> roles;
        try {
            roles = roleManager.listRolesWithGET(rootNode, startIndex, count, sortBy, sortOrder);
            assertEquals(((Role) roles.get(1)).getDisplayName(), "roleDisplayName1");
            assertEquals(((Role) roles.get(1)).getId(), "role1");
            result = SUCCESS;
        } catch (CharonException charonException) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithGETOperationNode")
    public Object[][] dataProviderForListRolesWithGETOperationNode() {

        return new Object[][]{
                {"Operation", 3, 2, null, null, SAMPLE_TENANT_DOMAIN2, "value", FAIL},
                {"Operation", 6, 4, null, null, SAMPLE_TENANT_DOMAIN2, null, FAIL},
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithGETOperationNode")
    public void testListRolesWithGETOperationNode(String nodeType, Integer startIndex, Integer count,
                                                  String sortBy, String sortOrder, String tenantDomain,
                                                  String attributes, String expected)
            throws BadRequestException, CharonException {

        Node rootNode = generateNodeBasedOnNodeType(nodeType, attributes);
        String result;
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        try {
            roleManager.listRolesWithGET(rootNode, startIndex, count, sortBy, sortOrder);
            result = SUCCESS;
        } catch (NotImplementedException notImplementedException) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithGETInvalidNode")
    public Object[][] dataProviderForListRolesWithGETInvalidNode() {

        return new Object[][]{
                {3, 2, null, null, SAMPLE_TENANT_DOMAIN, FAIL}
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithGETInvalidNode")
    public void testListRolesWithGETInvalidNode(Integer startIndex, Integer count, String sortBy, String sortOrder,
                                                String tenantDomain, String expected)
            throws BadRequestException, NotImplementedException {

        Node rootNode = new MockNode();
        String result;
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        try {
            roleManager.listRolesWithGET(rootNode, startIndex, count, sortBy, sortOrder);
            result = SUCCESS;
        } catch (CharonException charonException) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithGETPositive")
    public Object[][] dataProviderForListRolesWithGETPositive() {

        return new Object[][]{
                {null, 3, 2, null, null, SAMPLE_TENANT_DOMAIN, "value", SCIMCommonConstants.CO, SUCCESS},
                {"Expression", 6, 4, null, null, SAMPLE_TENANT_DOMAIN, "value", SCIMCommonConstants.EQ, SUCCESS},
                {"Expression", 9, 2, null, null, SAMPLE_TENANT_DOMAIN, "value", SCIMCommonConstants.SW, SUCCESS},
                {"Expression", 4, 4, null, null, SAMPLE_TENANT_DOMAIN2, "value", SCIMCommonConstants.EW, SUCCESS},
                {"Expression", 1, 4, null, null, SAMPLE_TENANT_DOMAIN2, "value", SCIMCommonConstants.CO, SUCCESS},
                {"Expression", 1, 4, null, null, SAMPLE_TENANT_DOMAIN, "value", "bad operation", BAD_REQUEST}
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithGETPositive")
    public void testListRolesWithGETPositive(String nodeType, Integer startIndex, Integer count, String sortBy,
                                             String sortOrder, String tenantDomain, String attributes, String operation,
                                             String expected)
            throws CharonException, IdentityRoleManagementException {

        Node rootNode = generateNodeBasedOnNodeType(nodeType, attributes, operation);
        String searchFilter;
        searchFilter = attributes;
        List<RoleBasicInfo> roleList = getDummyRoleBasicInfoList();

        when(mockRoleManagementService.getRoles(count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> roleList);
        when(mockRoleManagementService.getRoles(searchFilter, count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> roleList);
        String result;
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        try {
            roleManager.listRolesWithGET(rootNode, startIndex, count, sortBy, sortOrder);
            result = SUCCESS;
        } catch (NotImplementedException notImplementedException) {
            result = FAIL;
        } catch (BadRequestException e) {
            result = BAD_REQUEST;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForUpdateRoleUpdateRoleName")
    public Object[][] dataProviderForUpdateRoleUpdateRoleName() {

        return new Object[][]{
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_EXISTING_ROLE_NAME, SAMPLE_TENANT_DOMAIN, null,
                        CONFLICT},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN2, null,
                        SUCCESS},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME, SAMPLE_TENANT_DOMAIN2, null,
                        SUCCESS},
                {SAMPLE_NON_EXISTING_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        null, NOT_FOUND},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_SYSTEM_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN, null,
                        BAD_REQUEST},
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_INVALID_TENANT_DOMAIN,
                        null, UNEXPECTED_ERROR},
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN2,
                        "sql error", UNEXPECTED_ERROR}
        };
    }

    @Test(dataProvider = "dataProviderForUpdateRoleUpdateRoleName")
    public void testUpdateRoleUpdateRoleName(String roleId, String oldRoleName, String newRoleName, String tenantDomain,
                                             String sError, String expect)
            throws IdentityRoleManagementException, BadRequestException, CharonException {

        RoleBasicInfo roleBasicInfo = new RoleBasicInfo(roleId, newRoleName);
        Role[] oldAndNewRoles = getOldAndNewRoleDummies(roleId, oldRoleName, newRoleName);

        when(mockRoleManagementService.updateRoleName(anyString(), anyString(), anyString())).
                thenAnswer(invocationOnMock -> {
                    String newRoleNameArg = invocationOnMock.getArgumentAt(1, String.class);
                    String roleIdArg = invocationOnMock.getArgumentAt(0, String.class);
                    String tenantDomainArg = invocationOnMock.getArgumentAt(2, String.class);
                    if (EXISTING_ROLE_NAMES.contains(newRoleNameArg)) {
                        throw new IdentityRoleManagementClientException(ROLE_ALREADY_EXISTS.getCode(),
                                "Role name: " + newRoleNameArg +
                                        " is already there in the system. Please pick another role name.");
                    }
                    if (NON_EXISTING_ROLE_IDS.contains(roleIdArg)) {
                        throw new IdentityRoleManagementClientException(ROLE_NOT_FOUND.getCode(),
                                "Role id: " + roleIdArg + " does not exist in the system.");
                    }
                    if (SYSTEM_ROLES.contains(oldRoleName)) {
                        throw new IdentityRoleManagementClientException(RoleConstants.Error.OPERATION_FORBIDDEN.
                                getCode(),
                                "Invalid operation. Role: " + oldRoleName +
                                        " Cannot be renamed since it's a read only system role.");
                    }
                    unExpectedErrorThrower(tenantDomainArg, sError,
                            "Error while updating users to the role: %s in the tenantDomain: %s",
                            roleIdArg);
                    return roleBasicInfo;
                });
        when(mockRoleManagementService.updateUserListOfRole(
                eq(roleId), anyListOf(String.class), anyListOf(String.class), anyString())).
                thenReturn(roleBasicInfo);
        when(mockRoleManagementService.updateGroupListOfRole(eq(roleId), anyListOf(String.class),
                anyListOf(String.class), anyString())).
                thenReturn(roleBasicInfo);
        when(mockRoleManagementService.setPermissionsForRole(eq(roleId), anyListOf(String.class), anyString())).
                thenReturn(roleBasicInfo);

        SCIMRoleManager scimRoleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);

        String result;
        try {
            scimRoleManager.updateRole(oldAndNewRoles[0], oldAndNewRoles[1]);
            result = SUCCESS;
        } catch (ConflictException e) {
            result = CONFLICT;
        } catch (NotFoundException e) {
            result = NOT_FOUND;
        } catch (BadRequestException e) {
            result = BAD_REQUEST;
        } catch (CharonException e) {
            result = UNEXPECTED_ERROR;
        }
        assertEquals(expect, result);
    }

    @DataProvider(name = "dataProviderForUpdateRoleUpdateUserListOfRole")
    public Object[][] dataProviderForUpdateRoleUpdateUserListOfRole() {

        return new Object[][]{
                {SAMPLE_INVALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN, "",
                        null, BAD_REQUEST},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_INVALID_TENANT_DOMAIN,
                        "", null, UNEXPECTED_ERROR},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        "", "sql error", UNEXPECTED_ERROR},
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        "EMPTY_DELETED", null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        "EMPTY_NEW", null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        "EMPTY_BOTH", null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        "", null, SUCCESS}
        };
    }

    @Test(dataProvider = "dataProviderForUpdateRoleUpdateUserListOfRole")
    public void testUpdateRoleUpdateUserListOfRole(String roleId, String oldRoleName, String newRoleName,
                                                   String tenantDomain, String type, String sError, String expect)
            throws IdentityRoleManagementException, BadRequestException, CharonException,
            ConflictException, NotFoundException {

        RoleBasicInfo roleBasicInfo = new RoleBasicInfo(roleId, newRoleName);
        Role[] oldAndNewRoles = getOldAndNewRoleDummies(roleId, oldRoleName, newRoleName, type);

        when(mockRoleManagementService.updateRoleName(anyString(), anyString(), anyString())).
                thenReturn(roleBasicInfo);
        when(mockRoleManagementService.updateUserListOfRole(
                anyString(), anyListOf(String.class), anyListOf(String.class), anyString())).
                thenAnswer(invocationOnMock -> {
                    String roleIdArg = invocationOnMock.getArgumentAt(0, String.class);
                    String tenantDomainArg = invocationOnMock.getArgumentAt(3, String.class);
                    if (INVALID_ROLE_IDS.contains(roleIdArg)) {
                        String errorMessage =
                                "Invalid scenario. Multiple roles found for the given role name: " + roleIdArg
                                        + " and tenantDomain: " + tenantDomain;
                        throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
                    }
                    unExpectedErrorThrower(tenantDomainArg, sError,
                            "Error while updating users to the role: %s in the tenantDomain: %s", roleIdArg);
                    return roleBasicInfo;
                });
        when(mockRoleManagementService.updateGroupListOfRole(eq(roleId), anyListOf(String.class),
                anyListOf(String.class), anyString())).
                thenReturn(roleBasicInfo);
        when(mockRoleManagementService.setPermissionsForRole(eq(roleId), anyListOf(String.class), anyString())).
                thenReturn(roleBasicInfo);

        SCIMRoleManager scimRoleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            scimRoleManager.updateRole(oldAndNewRoles[0], oldAndNewRoles[1]);
            result = SUCCESS;
        } catch (BadRequestException e) {
            result = BAD_REQUEST;
        } catch (CharonException e) {
            result = UNEXPECTED_ERROR;
        }
        assertEquals(expect, result);
    }

    @DataProvider(name = "dataProviderForUpdateRoleUpdateGroupListOfRole")
    public Object[][] dataProviderForUpdateRoleUpdateGroupListOfRole() {

        return new Object[][]{
                {SAMPLE_INVALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN, "", null,
                        BAD_REQUEST},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_INVALID_TENANT_DOMAIN,
                        "", null, UNEXPECTED_ERROR},
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN2, "",
                        "sql error", UNEXPECTED_ERROR},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        "EMPTY_DELETED", null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        "EMPTY_NEW", null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        "EMPTY_BOTH", null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        "", null, SUCCESS},
        };
    }

    @Test(dataProvider = "dataProviderForUpdateRoleUpdateGroupListOfRole")
    public void testUpdateRoleUpdateGroupListOfRole(String roleId, String oldRoleName, String newRoleName,
                                                    String tenantDomain, String type, String sError, String expect)
            throws IdentityRoleManagementException, BadRequestException, CharonException, ConflictException,
            NotFoundException {

        RoleBasicInfo roleBasicInfo = new RoleBasicInfo(roleId, newRoleName);
        Role[] oldAndNewRoles = getOldAndNewRoleDummies(roleId, oldRoleName, newRoleName, type);
        when(mockRoleManagementService.updateRoleName(anyString(), anyString(), anyString())).
                thenReturn(roleBasicInfo);
        when(mockRoleManagementService.updateGroupListOfRole(
                anyString(), anyListOf(String.class), anyListOf(String.class), anyString())).
                thenAnswer(invocationOnMock -> {
                    String roleIdArg = invocationOnMock.getArgumentAt(0, String.class);
                    String tenantDomainArg = invocationOnMock.getArgumentAt(3, String.class);
                    if (INVALID_ROLE_IDS.contains(roleIdArg)) {
                        String errorMessage =
                                "Invalid scenario. Multiple roles found for the given role name: " + roleIdArg
                                        + " and tenantDomain: " + tenantDomain;
                        throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
                    }
                    unExpectedErrorThrower(tenantDomainArg, sError,
                            "Error while updating users to the role: %s in the tenantDomain: %s",
                            roleIdArg);
                    return roleBasicInfo;
                });
        when(mockRoleManagementService.updateUserListOfRole(eq(roleId), anyListOf(String.class),
                anyListOf(String.class), anyString())).
                thenReturn(roleBasicInfo);
        when(mockRoleManagementService.setPermissionsForRole(eq(roleId), anyListOf(String.class), anyString())).
                thenReturn(roleBasicInfo);

        SCIMRoleManager scimRoleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            scimRoleManager.updateRole(oldAndNewRoles[0], oldAndNewRoles[1]);
            result = SUCCESS;
        } catch (BadRequestException e) {
            result = BAD_REQUEST;
        } catch (CharonException e) {
            result = UNEXPECTED_ERROR;
        }
        assertEquals(expect, result);
    }

    @DataProvider(name = "dataProviderForUpdateRoleUpdatePermissionListOfRole")
    public Object[][] dataProviderForUpdateRoleUpdatePermissionListOfRole() {

        return new Object[][]{
                {SAMPLE_INVALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN, "",
                        null, BAD_REQUEST},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_SYSTEM_ROLE_NAME2, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN, "",
                        null, BAD_REQUEST},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_INVALID_TENANT_DOMAIN, "",
                        null, UNEXPECTED_ERROR},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_INVALID_TENANT_DOMAIN,
                        "NULL_NEW_PERMISSION", null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        "NULL_OLD_PERMISSION", null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        "EMPTY_NEW_PERMISSION", null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID2, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        "EMPTY_OLD_PERMISSION", null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        "ALL_EMPTY_PERMISSION", null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN,
                        "ALL_EQUAL_PERMISSION", null, SUCCESS},
                {SAMPLE_VALID_ROLE_ID, SAMPLE_VALID_ROLE_NAME, SAMPLE_VALID_ROLE_NAME2, SAMPLE_TENANT_DOMAIN2, "",
                        null, SUCCESS}
        };
    }

    @Test(dataProvider = "dataProviderForUpdateRoleUpdatePermissionListOfRole")
    public void testUpdateRoleUpdatePermissionListOfRole(String roleId, String oldRoleName, String newRoleName,
                                                         String tenantDomain, String permissionType, String sError,
                                                         String expect)
            throws IdentityRoleManagementException, BadRequestException, CharonException,
            ConflictException, NotFoundException {

        RoleBasicInfo roleBasicInfo = new RoleBasicInfo(roleId, newRoleName);
        Role[] oldAndNewRoles = getOldAndNewRoleDummies(roleId, oldRoleName, newRoleName, permissionType);
        when(mockRoleManagementService.updateRoleName(anyString(), anyString(), anyString())).
                thenReturn(roleBasicInfo);
        when(mockRoleManagementService.setPermissionsForRole(
                anyString(), anyListOf(String.class), anyString())).
                thenAnswer(invocationOnMock -> {
                    String roleIdArg = invocationOnMock.getArgumentAt(0, String.class);
                    String tenantDomainArg = invocationOnMock.getArgumentAt(2, String.class);
                    if (INVALID_ROLE_IDS.contains(roleIdArg)) {
                        String errorMessage =
                                "Invalid scenario. Multiple roles found for the given role name: " + roleIdArg
                                        + " and tenantDomain: " + tenantDomain;
                        throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
                    }
                    if (SYSTEM_ROLES.contains(oldRoleName)) {
                        throw new IdentityRoleManagementClientException(RoleConstants.Error.OPERATION_FORBIDDEN.
                                getCode(), "Invalid operation. Permissions cannot be modified in the role: "
                                + oldRoleName + " since it's a read only system role.");
                    }
                    unExpectedErrorThrower(tenantDomainArg, sError,
                            "Error while updating users to the role: %s in the tenantDomain: %s", roleIdArg);
                    return roleBasicInfo;
                });
        when(mockRoleManagementService.updateUserListOfRole(eq(roleId), anyListOf(String.class),
                anyListOf(String.class), anyString())).
                thenReturn(roleBasicInfo);
        when(mockRoleManagementService.updateGroupListOfRole(eq(roleId), anyListOf(String.class),
                anyListOf(String.class), anyString())).
                thenReturn(roleBasicInfo);

        SCIMRoleManager scimRoleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            scimRoleManager.updateRole(oldAndNewRoles[0], oldAndNewRoles[1]);
            result = SUCCESS;
        } catch (BadRequestException e) {
            result = BAD_REQUEST;
        } catch (CharonException e) {
            result = UNEXPECTED_ERROR;
        }
        assertEquals(expect, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithPOSTSortingNotSupport")
    public Object[][] dataProviderForListRolesWithPOSTSortingNotSupport() {

        return new Object[][]{
                {1, 3, "name", "ascending", SAMPLE_TENANT_DOMAIN, FAIL},
                {2, 2, null, "ascending", SAMPLE_TENANT_DOMAIN2, FAIL},
                {2, 5, "", "ascending", SAMPLE_TENANT_DOMAIN2, FAIL},
                {0, 0, "name", null, SAMPLE_TENANT_DOMAIN, FAIL},
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithPOSTSortingNotSupport")
    public void testListRolesWithPOSTSortingNotSupport(Integer startIndex, Integer count, String sortBy,
                                                       String sortOrder, String tenantDomain, String expected)
            throws BadRequestException, CharonException {

        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        String result;
        try {
            roleManager.listRolesWithPost(getDummySearchRequest(null, startIndex, count, sortBy, sortOrder));
            result = SUCCESS;
        } catch (NotImplementedException e) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithPOSTCountNullZero")
    public Object[][] dataProviderForListRolesWithPOSTCountNullZero() {

        return new Object[][]{
                {1, 0, null, null, SAMPLE_TENANT_DOMAIN2, SUCCESS}
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithPOSTCountNullZero")
    public void testListRolesWithPOSTCountNullZero(Integer startIndex, Integer count, String sortBy,
                                                   String sortOrder, String tenantDomain, String expected) {

        String result;
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        List<Object> roles;
        try {
            roles = roleManager.listRolesWithPost(getDummySearchRequest(null, startIndex, count, sortBy,
                    sortOrder));
            assertEquals(roles.size(), 0);
            result = SUCCESS;
        } catch (Exception e) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithPOSTInvalidLimit")
    public Object[][] dataProviderForListRolesWithPOSTInvalidLimit() {

        return new Object[][]{
                {"Expression", 1, -2, null, null, SAMPLE_TENANT_DOMAIN2, "value", FAIL},
                {null, 2, -5, null, null, SAMPLE_TENANT_DOMAIN, "attributes", FAIL},
                {null, 2, 6, null, null, SAMPLE_TENANT_DOMAIN, null, SUCCESS},
                {"Expression", 4, 6, null, null, SAMPLE_TENANT_DOMAIN2, "attributes", SUCCESS},
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithPOSTInvalidLimit")
    public void testListRolesWithPOSTInvalidLimit(String nodeType, Integer startIndex, Integer count,
                                                  String sortBy, String sortOrder, String tenantDomain,
                                                  String attributes, String expected)
            throws BadRequestException, IdentityRoleManagementException, NotImplementedException {

        Node rootNode = generateNodeBasedOnNodeType(nodeType, attributes);
        String searchFilter;
        searchFilter = attributes;
        List<RoleBasicInfo> roleList = getDummyRoleBasicInfoList();

        when(mockRoleManagementService.getRoles(count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> {
                    Integer countArg = invocationOnMock.getArgumentAt(0, Integer.class);

                    if (countArg != null && countArg < 0) {
                        String errorMessage =
                                "Invalid limit requested. Limit value should be greater than or equal to zero. limit: "
                                        + count;
                        throw new IdentityRoleManagementClientException(INVALID_LIMIT.getCode(), errorMessage);
                    }
                    return roleList;
                });
        when(mockRoleManagementService.getRoles(searchFilter, count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> {
                    Integer countArg = invocationOnMock.getArgumentAt(1, Integer.class);

                    if (countArg != null && countArg < 0) {
                        String errorMessage =
                                "Invalid limit requested. Limit value should be greater than or equal to zero. limit: "
                                        + count;
                        throw new IdentityRoleManagementClientException(INVALID_LIMIT.getCode(), errorMessage);
                    }
                    return roleList;
                });
        String result;
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        List<Object> roles;
        try {
            roles = roleManager.listRolesWithPost(getDummySearchRequest(rootNode, startIndex, count, sortBy,
                    sortOrder));
            assertEquals(((Role) roles.get(1)).getDisplayName(), "roleDisplayName1");
            assertEquals(((Role) roles.get(1)).getId(), "role1");
            result = SUCCESS;
        } catch (CharonException notImplementedException) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithPOSTInvalidOffset")
    public Object[][] dataProviderForListRolesWithPOSTInvalidOffset() {

        return new Object[][]{
                {"Expression", -1, 2, null, null, SAMPLE_TENANT_DOMAIN2, "value", FAIL},
                {null, -2, 4, null, null, SAMPLE_TENANT_DOMAIN2, "attributes", FAIL},
                {null, 2, 6, null, null, SAMPLE_TENANT_DOMAIN2, null, SUCCESS},
                {"Expression", 4, 2, null, null, SAMPLE_TENANT_DOMAIN, "attributes", SUCCESS},
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithPOSTInvalidOffset")
    public void testListRolesWithPOSTInvalidOffset(String nodeType, Integer startIndex, Integer count,
                                                   String sortBy, String sortOrder, String tenantDomain,
                                                   String attributes, String expected)
            throws BadRequestException, IdentityRoleManagementException, NotImplementedException {

        Node rootNode = generateNodeBasedOnNodeType(nodeType, attributes);
        String searchFilter;
        searchFilter = attributes;
        List<RoleBasicInfo> roleList = getDummyRoleBasicInfoList();
        when(mockRoleManagementService.getRoles(count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> {
                    Integer startIndexArg = invocationOnMock.getArgumentAt(1, Integer.class);
                    if (startIndexArg != null && startIndexArg < 0) {
                        String errorMessage =
                                "Invalid offset requested. Offset value should be zero or greater than zero. offSet: "
                                        + startIndexArg;
                        throw new IdentityRoleManagementClientException(INVALID_OFFSET.getCode(), errorMessage);
                    }
                    return roleList;
                });
        when(mockRoleManagementService.getRoles(searchFilter, count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> {
                    Integer startIndexArg = invocationOnMock.getArgumentAt(2, Integer.class);
                    if (startIndexArg != null && startIndexArg < 0) {
                        String errorMessage =
                                "Invalid offset requested. Offset value should be zero or greater than zero. offSet: "
                                        + startIndexArg;
                        throw new IdentityRoleManagementClientException(INVALID_OFFSET.getCode(), errorMessage);
                    }
                    return roleList;
                });
        String result;
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        List<Object> roles;
        try {
            roles = roleManager.listRolesWithPost(getDummySearchRequest(rootNode, startIndex, count,
                    sortBy, sortOrder));
            assertEquals(((Role) roles.get(1)).getDisplayName(), "roleDisplayName1");
            assertEquals(((Role) roles.get(1)).getId(), "role1");
            result = SUCCESS;
        } catch (CharonException charonException) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithPOSTUnExpectedServerError")
    public Object[][] dataProviderForListRolesWithPOSTUnExpectedServerError() {

        return new Object[][]{
                {"Expression", 3, 2, null, null, SAMPLE_INVALID_TENANT_DOMAIN, "value", null, FAIL},
                {null, 2, 6, null, null, SAMPLE_TENANT_DOMAIN, null, null, SUCCESS},
                {"Expression", 4, 2, null, null, SAMPLE_TENANT_DOMAIN, "attributes", null, SUCCESS},
                {null, 6, 4, null, null, SAMPLE_TENANT_DOMAIN2, "attributes", "sql error", FAIL},
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithPOSTUnExpectedServerError")
    public void testListRolesWithPOSTUnExpectedServerError(String nodeType, Integer startIndex, Integer count,
                                                           String sortBy, String sortOrder, String tenantDomain,
                                                           String attributes, String sError, String expected)
            throws BadRequestException, IdentityRoleManagementException, NotImplementedException {

        Node rootNode = generateNodeBasedOnNodeType(nodeType, attributes);
        String searchFilter;
        searchFilter = attributes;
        List<RoleBasicInfo> roleList = getDummyRoleBasicInfoList();

        when(mockRoleManagementService.getRoles(count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> {

                    String tenantDomainArg = invocationOnMock.getArgumentAt(4, String.class);
                    unExpectedErrorThrower(tenantDomainArg, sError,
                            "Error while listing roles in tenantDomain: ");
                    return roleList;
                });
        when(mockRoleManagementService.getRoles(searchFilter, count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> {
                    String tenantDomainArg = invocationOnMock.getArgumentAt(5, String.class);
                    unExpectedErrorThrower(tenantDomainArg, sError,
                            "Error while listing roles in tenantDomain: ");
                    return roleList;
                });
        String result;
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        List<Object> roles;
        try {
            roles = roleManager.listRolesWithPost(getDummySearchRequest(rootNode, startIndex, count, sortBy,
                    sortOrder));
            assertEquals(((Role) roles.get(1)).getDisplayName(), "roleDisplayName1");
            assertEquals(((Role) roles.get(1)).getId(), "role1");
            result = SUCCESS;
        } catch (CharonException charonException) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithPOSTOperationNode")
    public Object[][] dataProviderForListRolesWithPOSTOperationNode() {

        return new Object[][]{
                {"Operation", 3, 2, null, null, SAMPLE_TENANT_DOMAIN, "value", FAIL},
                {"Operation", 6, 4, null, null, SAMPLE_TENANT_DOMAIN, null, FAIL},
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithPOSTOperationNode")
    public void testListRolesWithPOSTOperationNode(String nodeType, Integer startIndex, Integer count, String sortBy,
                                                   String sortOrder, String tenantDomain, String attributes,
                                                   String expected)
            throws BadRequestException, CharonException {

        Node rootNode = generateNodeBasedOnNodeType(nodeType, attributes);
        String result;
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        try {
            roleManager.listRolesWithPost(getDummySearchRequest(rootNode, startIndex, count, sortBy, sortOrder));
            result = SUCCESS;
        } catch (NotImplementedException notImplementedException) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithPOSTInvalidNode")
    public Object[][] dataProviderForListRolesWithPOSTInvalidNode() {

        return new Object[][]{
                {3, 2, null, null, SAMPLE_TENANT_DOMAIN2, FAIL}
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithPOSTInvalidNode")
    public void testListRolesWithPOSTInvalidNode(Integer startIndex, Integer count, String sortBy,
                                                 String sortOrder, String tenantDomain, String expected)
            throws BadRequestException, NotImplementedException {

        Node rootNode = new MockNode();
        String result;
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        try {
            roleManager.listRolesWithPost(getDummySearchRequest(rootNode, startIndex, count, sortBy, sortOrder));
            result = SUCCESS;
        } catch (CharonException charonException) {
            result = FAIL;
        }
        assertEquals(expected, result);
    }

    @DataProvider(name = "dataProviderForListRolesWithPOSTPositive")
    public Object[][] dataProviderForListRolesWithPOSTPositive() {

        return new Object[][]{
                {null, 3, 2, null, null, SAMPLE_TENANT_DOMAIN, "value", SCIMCommonConstants.CO, SUCCESS},
                {"Expression", 6, 4, null, null, SAMPLE_TENANT_DOMAIN, "value", SCIMCommonConstants.EQ, SUCCESS},
                {"Expression", 9, 2, null, null, SAMPLE_TENANT_DOMAIN2, "value", SCIMCommonConstants.SW, SUCCESS},
                {"Expression", 4, 4, null, null, SAMPLE_TENANT_DOMAIN, "value", SCIMCommonConstants.EW, SUCCESS},
                {"Expression", 1, 4, null, null, SAMPLE_TENANT_DOMAIN2, "value", SCIMCommonConstants.CO, SUCCESS},
                {"Expression", 1, 4, null, null, SAMPLE_TENANT_DOMAIN2, "value", "bad operation", BAD_REQUEST},
        };
    }

    @Test(dataProvider = "dataProviderForListRolesWithPOSTPositive")
    public void testListRolesWithPOSTPositive(String nodeType, Integer startIndex, Integer count, String sortBy,
                                              String sortOrder, String tenantDomain, String attributes,
                                              String operation, String expected)
            throws CharonException, IdentityRoleManagementException {

        Node rootNode = generateNodeBasedOnNodeType(nodeType, attributes, operation);
        String searchFilter;
        searchFilter = attributes;
        List<RoleBasicInfo> roleList = getDummyRoleBasicInfoList();

        when(mockRoleManagementService.getRoles(count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> roleList);
        when(mockRoleManagementService.getRoles(searchFilter, count, startIndex, sortBy, sortOrder, tenantDomain)).
                thenAnswer(invocationOnMock -> roleList);
        String result;
        SCIMRoleManager roleManager = new SCIMRoleManager(mockRoleManagementService, tenantDomain);
        try {
            roleManager.listRolesWithPost(getDummySearchRequest(rootNode, startIndex, count, sortBy, sortOrder));
            result = SUCCESS;
        } catch (NotImplementedException notImplementedException) {
            result = FAIL;
        } catch (BadRequestException e) {
            result = BAD_REQUEST;
        }
        assertEquals(expected, result);
    }

    private Role[] getOldAndNewRoleDummies(String roleId, String oldRoleName, String newRoleName)
            throws BadRequestException, CharonException {

        return getOldAndNewRoleDummies(roleId, oldRoleName, newRoleName, "");
    }

    private Role[] getOldAndNewRoleDummies(String roleId, String oldRoleName, String newRoleName,
                                           String roleSelectionType)
            throws BadRequestException, CharonException {

        User u1 = new User();
        u1.setUserName("username1");
        u1.setId("7646b885-4207-4ca0-bc65-5df82272b6d1");
        User u2 = new User();
        u2.setUserName("username2");
        u2.setId("7646b885-4207-4ca0-bc65-5df82272b6d2");
        User u3 = new User();
        u3.setUserName("username3");
        u3.setId("7646b885-4207-4ca0-bc65-5df82272b6d3");
        User u4 = new User();
        u4.setUserName("username4");
        u4.setId("7646b885-4207-4ca0-bc65-5df82272b6d4");
        User u5 = new User();
        u5.setUserName("username5");
        u5.setId("7646b885-4207-4ca0-bc65-5df82272b6d5");
        //create groups
        Group group1 = new Group();
        group1.setDisplayName("groupName1");
        group1.setId("26d3a726-9c00-4f4c-8a4e-f5e310138081");
        group1.setMember(u1);
        Group group2 = new Group();
        group2.setDisplayName("groupName2");
        group2.setId("26d3a726-9c00-4f4c-8a4e-f5e310138082");
        group2.setMember(u2);
        Group group3 = new Group();
        group3.setDisplayName("groupName3");
        group3.setId("26d3a726-9c00-4f4c-8a4e-f5e310138083");
        group3.setMember(u3);
        Group group4 = new Group();
        group4.setDisplayName("groupName4");
        group4.setId("26d3a726-9c00-4f4c-8a4e-f5e310138084");
        group4.setMember(u4);
        Group group5 = new Group();
        group5.setDisplayName("groupName5");
        group5.setId("26d3a726-9c00-4f4c-8a4e-f5e310138085");
        group5.setMember(u5);
        //oldRole
        Role oldRole = new Role();
        oldRole.setId(roleId);
        oldRole.setDisplayName(oldRoleName);

        //newRole
        Role newRole = new Role();
        newRole.setId(roleId);
        newRole.setDisplayName(newRoleName);

        switch (roleSelectionType) {
            case "NULL_NEW_PERMISSION":
                newRole.setPermissions(null);
                break;
            case "NULL_OLD_PERMISSION":
                oldRole.setPermissions(null);
                break;
            case "EMPTY_NEW_PERMISSION":
                oldRole.setPermissions(Arrays.asList("permission", "usermgt", "security", "configure"));
                newRole.setPermissions(Collections.emptyList());
                break;
            case "EMPTY_OLD_PERMISSION":
                oldRole.setPermissions(Collections.emptyList());
                newRole.setPermissions(Arrays.asList("permission", "usermgt", "security", "configure"));
                break;
            case "ALL_EMPTY_PERMISSION":
                oldRole.setPermissions(Collections.emptyList());
                newRole.setPermissions(Collections.emptyList());
                break;
            case "ALL_EQUAL_PERMISSION":
                oldRole.setPermissions(Arrays.asList("permission", "usermgt", "configure", "admin"));
                newRole.setPermissions(Arrays.asList("permission", "usermgt", "configure", "admin"));
                break;
            case "EMPTY_DELETED":
                oldRole.setUser(u1);
                oldRole.setUser(u2);
                oldRole.setUser(u4);
                newRole.setUser(u1);
                newRole.setUser(u2);
                newRole.setUser(u4);
                newRole.setUser(u5);
                oldRole.setGroup(group1);
                oldRole.setGroup(group2);
                oldRole.setGroup(group4);
                newRole.setGroup(group1);
                newRole.setGroup(group2);
                newRole.setGroup(group4);
                newRole.setGroup(group5);
                break;
            case "EMPTY_NEW":
                oldRole.setUser(u1);
                oldRole.setUser(u2);
                oldRole.setUser(u4);
                newRole.setUser(u1);
                newRole.setUser(u2);
                oldRole.setGroup(group1);
                oldRole.setGroup(group2);
                oldRole.setGroup(group4);
                newRole.setGroup(group1);
                newRole.setGroup(group2);
                break;
            case "EMPTY_BOTH":
                oldRole.setUser(u1);
                oldRole.setUser(u2);
                oldRole.setUser(u4);
                newRole.setUser(u1);
                newRole.setUser(u2);
                newRole.setUser(u4);
                oldRole.setGroup(group1);
                oldRole.setGroup(group2);
                oldRole.setGroup(group4);
                newRole.setGroup(group1);
                newRole.setGroup(group2);
                newRole.setGroup(group4);
                break;
            default:
                oldRole.setPermissions(Arrays.asList("permission", "usermgt", "security", "configure"));
                newRole.setPermissions(Arrays.asList("permission", "usermgt", "configure", "admin"));
                oldRole.setUser(u1);
                oldRole.setUser(u2);
                oldRole.setUser(u2);
                oldRole.setUser(u4);
                newRole.setUser(u1);
                newRole.setUser(u2);
                newRole.setUser(u4);
                newRole.setUser(u5);
                oldRole.setGroup(group1);
                oldRole.setGroup(group2);
                oldRole.setGroup(group3);
                oldRole.setGroup(group4);
                newRole.setGroup(group1);
                newRole.setGroup(group2);
                newRole.setGroup(group4);
                newRole.setGroup(group5);
                break;
        }
        return new Role[]{oldRole, newRole};
    }

    private Role getDummyRole(String roleId, String roleDisplayName) throws BadRequestException, CharonException {

        Role role = new Role();
        User user = new User();
        user.setUserName("username");
        role.setUser(user);
        role.setDisplayName(roleDisplayName);
        role.setId(roleId);
        role.setPermissions(Arrays.asList("permission", "usermgt"));
        return role;
    }

    private org.wso2.carbon.identity.role.mgt.core.Role getDummyIdentityRole(String roleId, String roleName,
                                                                             String domain, String tenantDomain) {

        org.wso2.carbon.identity.role.mgt.core.Role role = new org.wso2.carbon.identity.role.mgt.core.Role();
        role.setId(roleId);
        role.setPermissions(Arrays.asList("permission", "usermgt"));
        role.setName(roleName);
        role.setDomain(domain);
        role.setTenantDomain(tenantDomain);
        role.setUsers(Arrays.asList(new UserBasicInfo("7646b885-4207-4ca0-bc65-5df82272b6d1", "username1"),
                new UserBasicInfo("7646b885-4207-4ca0-bc65-5df82272b6d2", "username2")));
        GroupBasicInfo groupBasicInfo1 = new GroupBasicInfo();
        groupBasicInfo1.setName("groupName1");
        groupBasicInfo1.setId("26d3a726-9c00-4f4c-8a4e-f5e310138081");
        GroupBasicInfo groupBasicInfo2 = new GroupBasicInfo();
        groupBasicInfo2.setName("groupName2");
        groupBasicInfo2.setId("26d3a726-9c00-4f4c-8a4e-f5e310138082");
        role.setGroups(Arrays.asList(groupBasicInfo1, groupBasicInfo2));
        return role;
    }

    private org.wso2.carbon.identity.role.mgt.core.Role getDummyIdentityRole(String roleId, String roleName,
                                                                             String domain, String tenantDomain,
                                                                             boolean isEmptyLists) {

        if (isEmptyLists) {
            org.wso2.carbon.identity.role.mgt.core.Role role = new org.wso2.carbon.identity.role.mgt.core.Role();
            role.setId(roleId);
            role.setPermissions(Arrays.asList("permission", "usermgt"));
            role.setName(roleName);
            role.setDomain(domain);
            role.setTenantDomain(tenantDomain);
            return role;
        } else {
            return getDummyIdentityRole(roleId, roleName, domain, tenantDomain);
        }
    }

    private void assertScimRoleFull(Role scimRole, String roleId) {

        assertEquals(scimRole.getId(), roleId);
        if (!scimRole.getUsers().isEmpty()) {
            assertEquals(scimRole.getUsers().get(0), "7646b885-4207-4ca0-bc65-5df82272b6d1");
        }
        assertEquals(scimRole.getPermissions().get(0), "permission");
        if (!scimRole.getGroups().isEmpty()) {
            assertEquals(scimRole.getGroups().get(0), "26d3a726-9c00-4f4c-8a4e-f5e310138081");
        }
        assertEquals(scimRole.getLocation(), DUMMY_SCIM_URL);
    }

    private List<RoleBasicInfo> getDummyRoleBasicInfoList() {

        return Arrays.asList(new RoleBasicInfo("role1", SAMPLE_VALID_ROLE_NAME),
                new RoleBasicInfo("role2", SAMPLE_VALID_ROLE_NAME2),
                new RoleBasicInfo("role3", SAMPLE_SYSTEM_ROLE_NAME));
    }

    private Node generateNodeBasedOnNodeType(String nodeType, String attributes) {

        return generateNodeBasedOnNodeType(nodeType, attributes, SCIMCommonConstants.EQ);
    }

    private Node generateNodeBasedOnNodeType(String nodeType, String attributes, String operation) {

        Node rootNode = null;
        if (nodeType != null && nodeType.equals("Expression")) {
            rootNode = new ExpressionNode();
            ((ExpressionNode) rootNode).setOperation(operation);
            ((ExpressionNode) rootNode).setAttributeValue("attributeValue");
            ((ExpressionNode) rootNode).setValue(attributes);
        } else if (nodeType != null && nodeType.equals("Operation")) {
            rootNode = new OperationNode("operation");
        }
        return rootNode;
    }

    private void unExpectedErrorThrower(String tenantDomainArg, String sError, String errorMessage)
            throws IdentityRoleManagementServerException {

        if (sError != null) {
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage + tenantDomainArg, new Error(sError));
        }
        if (tenantDomainArg.equals(SAMPLE_INVALID_TENANT_DOMAIN)) {
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    errorMessage + tenantDomainArg, new Error("invalid tenant domain"));
        }
    }

    private void unExpectedErrorThrower(String tenantDomainArg, String sError, String errorMessage, String roleIdArg)
            throws IdentityRoleManagementServerException {

        if (tenantDomainArg.equals(SAMPLE_INVALID_TENANT_DOMAIN)) {
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleIdArg, tenantDomainArg), new Error("invalid tenantDomain"));
        }
        if (sError != null) {
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleIdArg, tenantDomainArg), new Error(sError));
        }
    }

    private SearchRequest getDummySearchRequest(Node node, int startIndex, int count, String sortBy, String sortOrder) {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setFilter(node);
        searchRequest.setStartIndex(startIndex);
        searchRequest.setCount(count);
        searchRequest.setSortBy(sortBy);
        searchRequest.setSortOder(sortOrder);
        return searchRequest;
    }

    private static class MockNode extends Node {

    }
}

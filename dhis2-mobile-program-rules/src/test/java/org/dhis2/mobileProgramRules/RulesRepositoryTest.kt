package org.dhis2.mobileProgramRules

import kotlinx.coroutines.runBlocking
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitGroup
import org.hisp.dhis.android.core.user.UserRole
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

class RulesRepositoryTest {

    private lateinit var repository: RulesRepository

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Before
    fun setUp() {
        repository = RulesRepository(d2)
    }

    @Test
    fun `Should load supplementary data`() {
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .withOrganisationUnitGroups()
                .uid("org_unit_test")
                .blockingGet(),
        ) doReturn getTestOrgUnit()
        whenever(d2.userModule().userRoles().blockingGet()) doReturn getTestUserRoles()
        val supplData = runBlocking {
            repository.supplementaryData("org_unit_test")
        }
        assertTrue(supplData.isNotEmpty())
        assertTrue(supplData.containsKey("USER"))
        assertTrue(supplData.containsKey("org_unit_group_test_code"))
        assertTrue(supplData.containsKey("org_unit_group_test"))
        assertTrue(supplData.getOrElse("USER") { arrayListOf() }.contains("role1"))
        assertTrue(supplData.getOrElse("USER") { arrayListOf() }.contains("role2"))
        assertTrue(
            supplData.getOrElse("org_unit_group_test") { arrayListOf() }
                .contains("org_unit_test"),
        )
        assertTrue(
            supplData.getOrElse("org_unit_group_test_code") { arrayListOf() }
                .contains("org_unit_test"),
        )
    }

    @Test
    fun `Supplementary data should not include option groups with null code`() {
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .withOrganisationUnitGroups()
                .uid("org_unit_test")
                .blockingGet(),
        ) doReturn getTestOrgUnitWithNullCodeGroup()
        whenever(d2.userModule().userRoles().blockingGet()) doReturn getTestUserRoles()
        val supplData = runBlocking {
            repository.supplementaryData("org_unit_test")
        }

        assertTrue(supplData.isNotEmpty())

        assertTrue(supplData.containsKey("USER"))
        assertTrue(!supplData.containsKey("org_unit_group_test_code"))
        assertTrue(supplData.containsKey("org_unit_group_test"))
        assertTrue(supplData.getOrElse("USER") { arrayListOf() }.contains("role1"))
        assertTrue(supplData.getOrElse("USER") { arrayListOf() }.contains("role2"))
        assertTrue(
            supplData.getOrElse("org_unit_group_test") { arrayListOf() }
                .contains("org_unit_test"),
        )
        assertTrue(supplData.getOrElse("org_unit_group_test_code") { arrayListOf() }.isEmpty())
    }

    private fun getTestUserRoles(): List<UserRole> {
        return arrayListOf(
            UserRole.builder()
                .uid("role1")
                .name("roleName1")
                .code("roleCode1")
                .build(),
            UserRole.builder()
                .uid("role2")
                .name("roleName2")
                .code("roleCode2")
                .build(),
        )
    }

    private fun getTestOrgUnit(): OrganisationUnit {
        return OrganisationUnit.builder()
            .uid("org_unit_test")
            .organisationUnitGroups(arrayListOf(getTestOrgUnitGroup("org_unit_group_test_code")))
            .build()
    }

    private fun getTestOrgUnitWithNullCodeGroup(): OrganisationUnit {
        return OrganisationUnit.builder()
            .uid("org_unit_test")
            .organisationUnitGroups(arrayListOf(getTestOrgUnitGroup()))
            .build()
    }

    private fun getTestOrgUnitGroup(ouCode: String? = null): OrganisationUnitGroup? {
        return OrganisationUnitGroup.builder()
            .uid("org_unit_group_test")
            .code(ouCode)
            .build()
    }
}

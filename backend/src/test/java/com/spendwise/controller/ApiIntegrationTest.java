package com.spendwise.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Uji kontrak API.md terhadap aplikasi penuh + database seed. Aktif dengan -Dspendwise.it=true
 * (lihat DaoIntegrationTest). Setiap tes di-rollback.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfSystemProperty(named = "spendwise.it", matches = "true")
class ApiIntegrationTest {

    @Autowired MockMvc mvc;

    private MockHttpSession login(String username, String password) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/auth/login").session(session).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk());
        return session;
    }

    private MockHttpSession budi() throws Exception {
        return login("budi", "member12345");
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder b, MockHttpSession s, String body) {
        return b.session(s).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private int idOf(MockHttpSession s, String url, String jsonPathExpr) throws Exception {
        MvcResult r = mvc.perform(get(url).session(s)).andExpect(status().isOk()).andReturn();
        Object value = com.jayway.jsonpath.JsonPath.read(r.getResponse().getContentAsString(), jsonPathExpr);
        if (value instanceof java.util.List<?> list) {
            value = list.get(0);
        }
        return ((Number) value).intValue();
    }

    // ---------- auth ----------

    @Test
    void unauthenticatedRequestGets401InStandardFormat() throws Exception {
        mvc.perform(get("/api/wallets"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Silakan login terlebih dahulu"));
    }

    @Test
    void loginSuccessNeverLeaksPasswordHashAndMeWorks() throws Exception {
        MockHttpSession s = new MockHttpSession();
        mvc.perform(post("/api/auth/login").session(s).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"budi\",\"password\":\"member12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("MEMBER"))
                .andExpect(jsonPath("$.data.menuAccess[0]").value("DASHBOARD"))
                .andExpect(content().string(not(containsString("passwordHash"))))
                .andExpect(content().string(not(containsString("password_hash"))));
        mvc.perform(get("/api/auth/me").session(s))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("budi"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void wrongPasswordAndUnknownUserGiveSameMessage() throws Exception {
        for (String body : new String[]{"{\"username\":\"budi\",\"password\":\"salah\"}",
                "{\"username\":\"hantu\",\"password\":\"salah\"}"}) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Username atau password salah"));
        }
    }

    @Test
    void registerValidationDuplicateAndSuccess() throws Exception {
        String ok = "{\"username\":\"anggota_x\",\"fullName\":\"Anggota X\",\"email\":\"x@mail.com\","
                + "\"password\":\"password123\",\"confirmPassword\":\"password123\"}";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(ok.replace("\"password123\",\"confirmPassword\":\"password123\"",
                                "\"pendek\",\"confirmPassword\":\"pendek\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validasi gagal"))
                .andExpect(jsonPath("$.errors.password").value("Password minimal 8 karakter"));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(ok.replace("\"confirmPassword\":\"password123\"", "\"confirmPassword\":\"beda12345\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.confirmPassword").exists());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(ok.replace("anggota_x", "x!").replace("x@mail.com", "bukan-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.email").exists());

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(ok))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("MEMBER"));
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(ok))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username sudah dipakai"));

        MockHttpSession s = login("anggota_x", "password123");
        mvc.perform(get("/api/wallets").session(s))
                .andExpect(jsonPath("$.data.wallets", hasSize(1)))
                .andExpect(jsonPath("$.data.wallets[0].name").value("Tunai"))
                .andExpect(jsonPath("$.data.totalBalance").value(0.0));
    }

    @Test
    void changePasswordAndLogout() throws Exception {
        MockHttpSession s = budi();
        mvc.perform(json(put("/api/auth/password"), s,
                        "{\"oldPassword\":\"salah\",\"newPassword\":\"rahasia2026\",\"confirmPassword\":\"rahasia2026\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password lama tidak sesuai"));
        mvc.perform(json(put("/api/auth/password"), s,
                        "{\"oldPassword\":\"member12345\",\"newPassword\":\"rahasia2026\",\"confirmPassword\":\"rahasia2026\"}"))
                .andExpect(status().isOk());
        login("budi", "rahasia2026");
        mvc.perform(post("/api/auth/logout").session(s)).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").session(s)).andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile() throws Exception {
        mvc.perform(json(put("/api/auth/profile"), budi(), "{\"fullName\":\"Budi S\",\"email\":\"budi.s@mail.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Budi S"))
                .andExpect(jsonPath("$.data.username").value("budi"));
    }

    // ---------- otorisasi ----------

    @Test
    void memberCannotOpenAdminAndAdminCannotOpenMemberEndpoints() throws Exception {
        mvc.perform(get("/api/admin/users").session(budi())).andExpect(status().isForbidden());
        MockHttpSession admin = login("admin", "admin12345");
        mvc.perform(get("/api/wallets").session(admin)).andExpect(status().isForbidden());
        mvc.perform(get("/api/transactions").session(admin)).andExpect(status().isForbidden());
    }

    @Test
    void adminUserManagementAndDefaultCategories() throws Exception {
        MockHttpSession admin = login("admin", "admin12345");
        mvc.perform(get("/api/admin/users").session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(content().string(not(containsString("passwordHash"))));
        mvc.perform(json(patch("/api/admin/users/1/active"), admin, "{\"active\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tidak dapat menonaktifkan akun sendiri"));
        mvc.perform(json(patch("/api/admin/users/3/active"), admin, "{\"active\":false}")).andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"sari\",\"password\":\"member12345\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Akun Anda dinonaktifkan. Hubungi admin."));

        mvc.perform(json(post("/api/admin/categories"), admin, "{\"name\":\"Donasi\",\"type\":\"EXPENSE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.isDefault").value(true));
        mvc.perform(json(post("/api/admin/categories"), admin, "{\"name\":\"Donasi\",\"type\":\"EXPENSE\"}"))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/categories").session(admin)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.isDefault == false)]").isEmpty());
    }

    // ---------- dompet ----------

    @Test
    void walletsListCreateUpdateDeleteTransfer() throws Exception {
        MockHttpSession s = budi();
        mvc.perform(get("/api/wallets").session(s))
                .andExpect(jsonPath("$.data.totalBalance").value(1333000.0))
                .andExpect(jsonPath("$.data.wallets", hasSize(3)));

        MvcResult created = mvc.perform(json(post("/api/wallets"), s,
                        "{\"name\":\"Dana\",\"walletType\":\"E_WALLET\",\"initialBalance\":100000,\"balance\":777,\"ownerId\":99}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.balance").value(100000.0))
                .andExpect(jsonPath("$.data.ownerId").value(2))
                .andReturn();
        int id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.data.walletId");

        mvc.perform(json(post("/api/wallets"), s, "{\"name\":\"dana\",\"walletType\":\"CASH\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Nama dompet sudah dipakai"));

        mvc.perform(json(put("/api/wallets/" + id), s, "{\"name\":\"Dana Utama\",\"walletType\":\"E_WALLET\",\"balance\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Dana Utama"))
                .andExpect(jsonPath("$.data.balance").value(100000.0));

        int tunai = idOf(s, "/api/wallets", "$.data.wallets[0].walletId");
        mvc.perform(json(post("/api/wallets/transfer"), s,
                        "{\"fromWalletId\":" + id + ",\"toWalletId\":" + tunai + ",\"amount\":40000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.from.balance").value(60000.0))
                .andExpect(jsonPath("$.data.to.balance").value(190000.0));
        mvc.perform(json(post("/api/wallets/transfer"), s,
                        "{\"fromWalletId\":" + id + ",\"toWalletId\":" + id + ",\"amount\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dompet asal dan tujuan tidak boleh sama"));
        mvc.perform(json(post("/api/wallets/transfer"), s,
                        "{\"fromWalletId\":" + id + ",\"toWalletId\":" + tunai + ",\"amount\":99999999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Saldo dompet tidak mencukupi"));

        mvc.perform(delete("/api/wallets/" + tunai).session(s))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Dompet masih memiliki transaksi"));
        mvc.perform(delete("/api/wallets/" + id).session(s)).andExpect(status().isOk());
        mvc.perform(delete("/api/wallets/" + id).session(s)).andExpect(status().isNotFound());
    }

    // ---------- kategori ----------

    @Test
    void categoriesListFilterCreateAndCannotForgeDefault() throws Exception {
        MockHttpSession s = budi();
        mvc.perform(get("/api/categories?type=EXPENSE").session(s))
                .andExpect(jsonPath("$.data", hasSize(10)));
        mvc.perform(get("/api/categories?type=BOGUS").session(s)).andExpect(status().isBadRequest());

        mvc.perform(json(post("/api/categories"), s,
                        "{\"name\":\"Hobi\",\"type\":\"EXPENSE\",\"isDefault\":true,\"ownerId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.isDefault").value(false))
                .andExpect(jsonPath("$.data.ownerId").value(2));
        mvc.perform(json(post("/api/categories"), s, "{\"name\":\"makan & minum\",\"type\":\"EXPENSE\"}"))
                .andExpect(status().isConflict());
        int makan = idOf(s, "/api/categories?type=EXPENSE", "$.data[0].categoryId");
        mvc.perform(delete("/api/categories/" + makan).session(s)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Kategori default tidak dapat dihapus"));
    }

    // ---------- transaksi ----------

    @Test
    void transactionListMatchesSeedWithTotals() throws Exception {
        mvc.perform(get("/api/transactions?from=2026-10-01&to=2026-10-31&size=5").session(budi()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(5)))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.totalItems").value(19))
                .andExpect(jsonPath("$.data.totalPages").value(4))
                .andExpect(jsonPath("$.data.totalIncome").value(5500000.0))
                .andExpect(jsonPath("$.data.totalExpense").value(3582000.0))
                .andExpect(jsonPath("$.data.items[0].category.name").exists())
                .andExpect(jsonPath("$.data.items[0].wallet.name").exists());
    }

    @Test
    void transactionCreateUpdateDeleteChangeBalanceAndValidate() throws Exception {
        MockHttpSession s = budi();
        int tunai = idOf(s, "/api/wallets", "$.data.wallets[0].walletId");
        int makan = idOf(s, "/api/categories?type=EXPENSE", "$.data[0].categoryId");
        String body = "{\"type\":\"EXPENSE\",\"amount\":45000,\"date\":\"2026-10-28\",\"categoryId\":" + makan
                + ",\"walletId\":" + tunai + ",\"description\":\"Makan siang\",\"paymentMethod\":\"QRIS\",\"isRecurring\":false}";

        MvcResult created = mvc.perform(json(post("/api/transactions"), s, body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("EXPENSE"))
                .andExpect(jsonPath("$.data.paymentMethod").value("QRIS"))
                .andExpect(jsonPath("$.data.isRecurring").value(false))
                .andExpect(jsonPath("$.data.source").doesNotExist())
                .andReturn();
        int id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.data.transactionId");
        mvc.perform(get("/api/wallets").session(s))
                .andExpect(jsonPath("$.data.wallets[0].balance").value(105000.0));

        mvc.perform(json(put("/api/transactions/" + id), s, body.replace("45000", "20000")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(20000.0));
        mvc.perform(get("/api/wallets").session(s))
                .andExpect(jsonPath("$.data.wallets[0].balance").value(130000.0));

        mvc.perform(delete("/api/transactions/" + id).session(s)).andExpect(status().isOk());
        mvc.perform(get("/api/wallets").session(s))
                .andExpect(jsonPath("$.data.wallets[0].balance").value(150000.0));
        mvc.perform(delete("/api/transactions/" + id).session(s)).andExpect(status().isNotFound());

        mvc.perform(json(post("/api/transactions"), s, body.replace("45000", "99999999")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Saldo dompet tidak mencukupi"));
        mvc.perform(json(post("/api/transactions"), s, body.replace("45000", "0").replace("2026-10-28", "2099-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").value("Nominal harus lebih dari 0"));
        mvc.perform(json(post("/api/transactions"), s, body.replace("2026-10-28", "2099-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tanggal tidak boleh di masa depan"));
        mvc.perform(json(post("/api/transactions"), s, "{\"type\":\"EXPENSE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.walletId").exists());
        mvc.perform(json(post("/api/transactions"), s, "{ bukan json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void anotherMemberCannotSeeOrChangeTransactions() throws Exception {
        MockHttpSession sari = login("sari", "member12345");
        mvc.perform(get("/api/transactions?from=2026-01-01&to=2026-12-31").session(sari))
                .andExpect(jsonPath("$.data.totalItems").value(0));
        mvc.perform(delete("/api/transactions/1").session(sari)).andExpect(status().isNotFound());
    }

    // ---------- anggaran, notifikasi, target ----------

    @Test
    void budgetsListCreateUpdateDelete() throws Exception {
        MockHttpSession s = budi();
        mvc.perform(get("/api/budgets?month=2026-10").session(s))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.month").value("2026-10"))
                .andExpect(jsonPath("$.data.budgets", hasSize(4)))
                .andExpect(jsonPath("$.data.budgets[?(@.status == 'EXCEEDED')]", hasSize(1)))
                .andExpect(jsonPath("$.data.budgets[?(@.status == 'WARNING')]", hasSize(1)));
        mvc.perform(get("/api/budgets?month=oktober").session(s)).andExpect(status().isBadRequest());

        int pendidikan = idOf(s, "/api/categories?type=EXPENSE", "$.data[?(@.name == 'Pendidikan')].categoryId");
        int gaji = idOf(s, "/api/categories?type=INCOME", "$.data[?(@.name == 'Gaji')].categoryId");
        mvc.perform(json(post("/api/budgets"), s,
                        "{\"categoryId\":" + gaji + ",\"month\":\"2026-10\",\"targetAmount\":100000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Anggaran hanya untuk kategori pengeluaran"));

        MvcResult created = mvc.perform(json(post("/api/budgets"), s,
                        "{\"categoryId\":" + pendidikan + ",\"month\":\"2026-10\",\"targetAmount\":500000,\"spentAmount\":999}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Anggaran Pendidikan"))
                .andExpect(jsonPath("$.data.period").value("2026-10"))
                .andExpect(jsonPath("$.data.spentAmount").value(0.0))
                .andExpect(jsonPath("$.data.status").value("SAFE"))
                .andReturn();
        int id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.data.planId");
        mvc.perform(json(post("/api/budgets"), s,
                        "{\"categoryId\":" + pendidikan + ",\"month\":\"2026-10\",\"targetAmount\":1}"))
                .andExpect(status().isConflict());
        mvc.perform(json(put("/api/budgets/" + id), s, "{\"targetAmount\":750000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.targetAmount").value(750000.0))
                .andExpect(jsonPath("$.data.remainingAmount").value(750000.0));
        mvc.perform(delete("/api/budgets/" + id).session(s)).andExpect(status().isOk());
    }

    @Test
    void expenseTriggersNotificationThroughObserverWiring() throws Exception {
        MockHttpSession s = budi();
        int bca = idOf(s, "/api/wallets", "$.data.wallets[?(@.name == 'BCA')].walletId");
        int pendidikan = idOf(s, "/api/categories?type=EXPENSE", "$.data[?(@.name == 'Pendidikan')].categoryId");
        mvc.perform(json(post("/api/budgets"), s,
                "{\"categoryId\":" + pendidikan + ",\"month\":\"2026-10\",\"targetAmount\":100000}"));
        mvc.perform(get("/api/notifications").session(s)).andExpect(jsonPath("$.data.unreadCount").value(2));

        mvc.perform(json(post("/api/transactions"), s, "{\"type\":\"EXPENSE\",\"amount\":85000,\"date\":\"2026-10-28\","
                + "\"categoryId\":" + pendidikan + ",\"walletId\":" + bca + "}")).andExpect(status().isCreated());

        mvc.perform(get("/api/notifications").session(s))
                .andExpect(jsonPath("$.data.unreadCount").value(3))
                .andExpect(jsonPath("$.data.items[0].level").value("WARNING"))
                .andExpect(jsonPath("$.data.items[0].message")
                        .value("Anggaran Pendidikan Oktober 2026 sudah terpakai 85%"));
        mvc.perform(post("/api/notifications/read-all").session(s)).andExpect(status().isOk());
        mvc.perform(get("/api/notifications").session(s)).andExpect(jsonPath("$.data.unreadCount").value(0));
    }

    @Test
    void goalsListCreateContributeCancel() throws Exception {
        MockHttpSession s = budi();
        mvc.perform(get("/api/goals").session(s))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data[0].progressPercentage").value(31.25))
                .andExpect(jsonPath("$.data[0].recommendedMonthlySaving").exists())
                .andExpect(jsonPath("$.data[0].daysRemaining").exists());
        int goal = idOf(s, "/api/goals", "$.data[0].planId");
        int bca = idOf(s, "/api/wallets", "$.data.wallets[?(@.name == 'BCA')].walletId");

        mvc.perform(json(post("/api/goals/" + goal + "/contribute"), s, "{\"walletId\":" + bca + ",\"amount\":500000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.goal.currentAmount").value(3000000.0))
                .andExpect(jsonPath("$.data.wallet.balance").value(490000.0));
        mvc.perform(json(post("/api/goals/" + goal + "/contribute"), s, "{\"walletId\":" + bca + ",\"amount\":99999999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nominal melebihi sisa target"));

        mvc.perform(json(post("/api/goals"), s,
                        "{\"name\":\"Liburan\",\"targetAmount\":3000000,\"deadline\":\"2020-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tenggat harus di masa depan"));
        mvc.perform(json(post("/api/goals"), s,
                        "{\"name\":\"Liburan\",\"targetAmount\":3000000,\"deadline\":\"2099-01-01\",\"currentAmount\":2999999}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.currentAmount").value(0.0))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mvc.perform(post("/api/goals/" + goal + "/cancel").session(s))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        mvc.perform(get("/api/wallets").session(s))
                .andExpect(jsonPath("$.data.wallets[0].balance").value(3150000.0));
    }

    // ---------- laporan ----------

    @Test
    void dashboardCategoryAndPeriodReports() throws Exception {
        MockHttpSession s = budi();
        mvc.perform(get("/api/reports/dashboard").session(s))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalBalance").value(1333000.0))
                .andExpect(jsonPath("$.data.activeGoals").value(1));
        mvc.perform(get("/api/reports/category?from=2026-10-01&to=2026-10-31").session(s))
                .andExpect(jsonPath("$.data.totalIncome").value(5500000.0))
                .andExpect(jsonPath("$.data.totalExpense").value(3582000.0))
                .andExpect(jsonPath("$.data.netBalance").value(1918000.0))
                .andExpect(jsonPath("$.data.topExpenseCategory").value("Kos & Sewa"))
                .andExpect(jsonPath("$.data.rows[0].category").value("Kos & Sewa"))
                .andExpect(jsonPath("$.data.rows[0].percentage").value(36.29));
        mvc.perform(get("/api/reports/period?type=WEEKLY&from=2026-10-01&to=2026-10-31").session(s))
                .andExpect(jsonPath("$.data.periodType").value("WEEKLY"))
                .andExpect(jsonPath("$.data.rows[0].label").value("2026-W40"))
                .andExpect(jsonPath("$.data.averageDailyExpense").value(115548.39))
                .andExpect(jsonPath("$.data.comparisonWithPrevious").exists());
        mvc.perform(get("/api/reports/category?from=2026-10-31&to=2026-10-01").session(s))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/reports/period?type=HOURLY").session(s)).andExpect(status().isBadRequest());
    }

    @Test
    void sariDashboardIsEmptyAndSavingRateIsNull() throws Exception {
        mvc.perform(get("/api/reports/dashboard").session(login("sari", "member12345")))
                .andExpect(jsonPath("$.data.totalBalance").value(0.0))
                .andExpect(jsonPath("$.data.savingRate").doesNotExist());
    }

    @Test
    void exportCsvHasBomSemicolonAndFilename() throws Exception {
        MvcResult r = mvc.perform(get("/api/reports/export?kind=category&from=2026-10-01&to=2026-10-31").session(budi()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        containsString("spendwise_laporan-kategori_2026-10-01_2026-10-31.csv")))
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andReturn();
        byte[] bytes = r.getResponse().getContentAsByteArray();
        assertEquals((byte) 0xEF, bytes[0]);
        assertEquals((byte) 0xBB, bytes[1]);
        assertEquals((byte) 0xBF, bytes[2]);
        String csv = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("﻿Kategori;Total;Persentase\r\n"));
        assertTrue(csv.contains("Kos & Sewa;1300000;36,29\r\n"));
        assertTrue(csv.contains("Total Pemasukan;5500000;\r\n"));

        mvc.perform(get("/api/reports/export?kind=period&periodType=WEEKLY&from=2026-10-01&to=2026-10-31").session(budi()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("laporan-periode_2026-10-01_2026-10-31")));
        mvc.perform(get("/api/reports/export?kind=lain").session(budi())).andExpect(status().isBadRequest());
    }
}

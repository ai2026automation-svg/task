package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AppsScriptClient(private val getBaseUrl: () -> String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun callAction(action: String, token: String? = null, params: Map<String, Any?> = emptyMap()): Result<JSONObject> {
        return withContext(Dispatchers.IO) {
            val url = getBaseUrl().trim()
            if (url.isBlank()) {
                return@withContext Result.failure(IllegalStateException("Google Apps Script Web App URL is not set. Please set it in App Settings or use Offline Mode."))
            }

            try {
                val jsonPayload = JSONObject().apply {
                    put("action", action)
                    if (!token.isNullOrBlank()) {
                        put("token", token)
                    }
                    for ((k, v) in params) {
                        when (v) {
                            null -> put(k, JSONObject.NULL)
                            is Map<*, *> -> put(k, JSONObject(v))
                            is List<*> -> put(k, JSONArray(v))
                            else -> put(k, v)
                        }
                    }
                }

                val requestBody = jsonPayload.toString().toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .header("Accept", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    val bodyString = response.body?.string() ?: ""
                    if (!response.isSuccessful && response.code != 302) {
                        return@withContext Result.failure(Exception("Apps Script HTTP ${response.code}: $bodyString"))
                    }

                    if (bodyString.isBlank()) {
                        return@withContext Result.failure(Exception("Empty response from Google Apps Script Web App"))
                    }

                    try {
                        val json = JSONObject(bodyString)
                        if (json.optBoolean("ok", true) || json.has("status") || json.has("rows") || json.has("total")) {
                            Result.success(json)
                        } else {
                            val error = json.optString("error", "Apps Script error")
                            Result.failure(Exception(error))
                        }
                    } catch (parseEx: Exception) {
                        Result.failure(Exception("Invalid JSON response from Apps Script: ${bodyString.take(200)}..."))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun testConnection(): Result<String> {
        return withContext(Dispatchers.IO) {
            val url = getBaseUrl().trim()
            if (url.isBlank()) {
                return@withContext Result.failure(IllegalStateException("Please enter your Google Apps Script Web App URL."))
            }

            try {
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful || response.code == 302) {
                        Result.success("Connected successfully! (HTTP ${response.code})")
                    } else {
                        Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    companion object {
        /**
         * Suggested doPost dispatcher snippet to add to Code.gs in Google Apps Script
         * to receive API calls from this Android app.
         */
        const val CODE_GS_DOPROST_SNIPPET = """
function doPost(e) {
  try {
    var body = e.postData ? e.postData.contents : '';
    var req = body ? JSON.parse(body) : {};
    var action = req.action;
    var token = req.token || '';
    var res = {};

    switch(action) {
      case 'requestLoginCode':
        res = requestLoginCode(req.email);
        break;
      case 'verifyLoginCode':
        res = verifyLoginCode(req.email, req.code);
        break;
      case 'resumeSession':
        res = resumeSession(token);
        break;
      case 'logout':
        res = logout(token);
        break;
      case 'getDashboard':
        res = getDashboard(token, req.opts || {});
        break;
      case 'listRecurring':
        res = listRecurring(token, req.opts || {});
        break;
      case 'listOneTime':
        res = listOneTime(token, req.opts || {});
        break;
      case 'assignRecurringTask':
        res = assignRecurringTask(token, req.data || {});
        break;
      case 'assignOneTimeTask':
        res = assignOneTimeTask(token, req.data || {});
        break;
      case 'completeRecurring':
        res = completeRecurring(token, req.rowNum, req.payload || {});
        break;
      case 'completeOneTime':
        res = completeOneTime(token, req.rowNum, req.payload || {});
        break;
      case 'editRecurring':
        res = editRecurring(token, req.rowNum, req.sno, req.data || {});
        break;
      case 'editOneTimeTask':
        res = editOneTimeTask(token, req.rowNum, req.taskId, req.data || {});
        break;
      case 'deleteRecurringSeries':
        res = deleteRecurringSeries(token, req.rowNum, req.sno);
        break;
      case 'deleteOneTimeTask':
        res = deleteOneTimeTask(token, req.rowNum, req.taskId);
        break;
      case 'listUsers':
        res = { ok: true, users: listUsers(token) };
        break;
      case 'addUser':
        res = addUser(token, req.user || {});
        break;
      case 'updateUserRole':
        res = updateUserRole(token, req.rowNum, req.role);
        break;
      case 'deleteUser':
        res = deleteUser(token, req.rowNum);
        break;
      case 'getSettings':
        res = { ok: true, settings: getSettings(token) };
        break;
      case 'saveSettings':
        res = saveSettings(token, req.settings || {});
        break;
      default:
        res = { ok: false, error: 'Unknown action: ' + action };
    }

    return ContentService.createTextOutput(JSON.stringify(res))
      .setMimeType(ContentService.MimeType.JSON);
  } catch(err) {
    return ContentService.createTextOutput(JSON.stringify({ ok: false, error: err.message || String(err) }))
      .setMimeType(ContentService.MimeType.JSON);
  }
}
"""
    }
}

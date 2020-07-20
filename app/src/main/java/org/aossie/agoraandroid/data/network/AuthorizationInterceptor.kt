package org.aossie.agoraandroid.data.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import org.aossie.agoraandroid.data.db.AppDatabase
import org.aossie.agoraandroid.data.db.PreferenceProvider
import org.aossie.agoraandroid.data.db.entities.User
import org.aossie.agoraandroid.data.network.responses.AuthResponse
import org.aossie.agoraandroid.utilities.Coroutines
import org.aossie.agoraandroid.utilities.SessionExpirationException
import org.json.JSONObject

class AuthorizationInterceptor(
  private val prefs: PreferenceProvider,
  private val appDatabase: AppDatabase,
  private val client: Client
) : Interceptor, ApiRequest() {

  override fun intercept(chain: Interceptor.Chain): Response {
    val mainResponse = chain.proceed(chain.request())

    if (prefs.getIsLoggedIn()) {
      // if response code is 401 or 403, network call has encountered authentication error
      if (mainResponse.code() == 401 || mainResponse.code() == 403) {
        Coroutines.io{
          var user = appDatabase.getUserDao().getUserInfo()
          if (prefs.getIsFacebookUser()) {
            val response = client.api.facebookLogin(prefs.getFacebookAccessToken())
            if (response.isSuccessful) {
              // save new access token
              prefs.setCurrentToken(response.body()!!.token)
              user.token = response.body()!!.token
              user.expiredAt = response.body()!!.expiresOn
            }
          } else {
            val jsonObject = JSONObject()
            jsonObject.put("identifier", user.username)
            jsonObject.put("password", user.password)
            val loginResponse = client.api.logIn(jsonObject.toString())
            if (loginResponse.isSuccessful) {
              val authResponse: AuthResponse? = loginResponse.body()
              authResponse.let {
                user = User(
                    it?.username, it?.email, it?.firstName, it?.lastName,
                    it?.towFactorAuthentication,
                    it?.token?.token, it?.token?.expiresOn, user.password
                )
                Log.d("friday", authResponse.toString())
              }
            }
          }
          appDatabase.getUserDao().replace(user)
          prefs.setIsLoggedIn(true)
          prefs.setCurrentToken(user.token)
        }
        throw SessionExpirationException("Your session was expired. Please try again")
      }
    }
    return mainResponse
  }
}
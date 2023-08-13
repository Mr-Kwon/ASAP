package com.d103.asaf.ui.join

import android.content.Context
import android.net.Uri
import android.os.Build.VERSION_CODES.P
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d103.asaf.common.model.dto.Classinfo
import com.d103.asaf.common.model.dto.Member
import com.d103.asaf.common.util.RetrofitUtil
import com.d103.asaf.common.util.RetrofitUtil.Companion.memberService
import com.d103.asaf.ui.sign.SignDrawFragment.Companion.regionCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Date

private const val TAG = "JoinFragmentViewModel_cjw"
class JoinFragmentViewModel : ViewModel() {

    // email 중복 체크 여부를 담는 MutableLiveData
    private val _isEmailDuplicated = MutableLiveData(false)
    val isEmailDuplicated: LiveData<Boolean> get() = _isEmailDuplicated


    // 회원가입 성공 여부를 담는 MutableLiveData
    private val _isJoinSuccessful = MutableLiveData(false)
    val isJoinSuccessful: LiveData<Boolean> get() = _isJoinSuccessful

    // 반 배정 성공 여부
    private val _isSetClassSuccessful = MutableLiveData(false)
    val isSetClassSuccessful: LiveData<Boolean> get() = _isSetClassSuccessful


    suspend fun signup(member: Member) {
        val userInfo = "이름: ${member.memberName}, 이메일: ${member.memberEmail}, 비밀번호: ${member.memberPassword}, " +
                "생년월일: ${member.birthDate}, 추가정보: ${member.memberInfo}, 학번: ${member.studentNumber}, 전화번호: ${member.phoneNumber}"
        Log.d(TAG, "signup: $userInfo")

        try {
            _isJoinSuccessful.value = RetrofitUtil.memberService.insert(member)
        } catch (e: Exception) {
            Log.d(TAG, "signup: 오류 발생")
            Log.d(TAG, "signup: $e")
        }
    }

    suspend fun signedMem(email: String, generationCode: Int, regionCode: Int, classCode: Int): Int {
        var id = 0
        try {
            val response = memberService.getUserInfo(email)
            if (response.isSuccessful) {
                val member = response.body()
                Log.d(TAG, "signedMem: ${member!!.id}")
                id = member.id

                val classInfoResponse = memberService.setClass(id, classCode, regionCode, generationCode)

                if (classInfoResponse.isSuccessful && classInfoResponse.body()=="true") {
                    Log.d(TAG, "setClass: 성공 !")
                } else {
                    Log.d(TAG, "setClass: 실패 !")
                    Log.d(TAG, "signedMem: ${classInfoResponse.errorBody()}")
                    memberService.setClass(id, generationCode, regionCode, classCode)
                }
            } else {
                // 서버 통신 실패
                Log.d(TAG, "signedMem: 서버 통신 실패")
            }
        } catch (e: Exception) {
            Log.d(TAG, "signedMem 에러 발생 : $e")
        }
        return id
    }

    fun validateInputs(
        member: Member,
        confirmPassword: String,
        uri: Uri?
    ): Boolean {
        // 입력 값의 유효성을 검사합니다.
        if (member.memberName.isBlank() || member.memberEmail.isBlank() ||
            member.memberPassword.isBlank() || confirmPassword.isBlank() ||
            member.birthDate.isBlank() || member.memberInfo.isBlank() ||
            member.phoneNumber.isBlank() || member.studentNumber.toString().isBlank() || uri == null) {
            // 입력값 중 하나라도 비어있을 경우 false를 반환합니다.
            return false
        }
        // 이메일 형식이 유효한지 검사합니다.
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(member.memberEmail).matches()) {
            return false
        }
        // 비밀번호와 확인 비밀번호가 일치하는지 확인합니다.
        if (member.memberPassword != confirmPassword) {
            // 비밀번호와 확인 비밀번호가 일치하지 않을 경우 false를 반환합니다.
            return false
        }
        // 학번이 유효한지 검사합니다.
        if (member.studentNumber == 0) {
            return false
        }
        // 핸드폰 번호가 유효한지 검사합니다.
        if (member.phoneNumber.length != 11) {
            return false
        }
        // 모든 유효성 검사를 통과한 경우 true를 반환합니다.
        return true
    }

    suspend fun uploadProfileImage(context: Context, email: String, imageUri: Uri) {
        val file = File(imageUri.path)
        val multipartImage = createRequestBodyFromFile(context, file)

        val profileImagePart = createMultipartFromUri(context, imageUri)
        val emailRequestBody = RequestBody.create(okhttp3.MultipartBody.FORM, email)

        try {
            // RetrofitUtil을 사용하여 서버에 프로필 이미지 업로드 요청
            val response = memberService.uploadProfileImage(emailRequestBody, profileImagePart!!)
            if (response.isSuccessful && response.body() != null && response.body() == true) {
                // 이미지 업로드 성공 처리
                Log.d(TAG, "uploadProfileImage: 이미지 업로드 성공")
            } else {
                // 이미지 업로드 실패 처리
                Log.e(TAG, "uploadProfileImage: 이미지 업로드 실패")
            }
        } catch (e: Exception) {
            // 예외 처리 로직
            Log.e(TAG, "uploadProfileImage: Error", e)
        }
    }

    private fun createMultipartFromUri(context: Context, uri: Uri): MultipartBody.Part? {
        val file: File = getFileFromUri(context, uri) ?: return null
        // 파일을 가져오지 못한 경우 처리할 로직
        val requestFile: RequestBody = createRequestBodyFromFile(context, file)
        return MultipartBody.Part.createFormData("file", file.name, requestFile)
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val filePath = uriToFilePath(context, uri)
        return if (filePath != null) File(filePath) else null
    }

    private fun uriToFilePath(context: Context, uri: Uri): String? {
        Log.d(TAG, "URI-join:$uri")
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val filePath = cursor?.getString(columnIndex!!)
        cursor?.close()
        return filePath
    }

    private fun createRequestBodyFromFile(context: Context, file: File): RequestBody {
        val MEDIA_TYPE_IMAGE = "image/png".toMediaTypeOrNull()
        val inputStream: InputStream = FileInputStream(file)
        val byteArray = inputStream.readBytes()
        return RequestBody.create(MEDIA_TYPE_IMAGE, byteArray)
    }
}

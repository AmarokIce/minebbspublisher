package club.someoneice.mbp

import club.someoneice.json.JSON
import club.someoneice.json.node.JsonNode
import club.someoneice.json.node.MapNode
import club.someoneice.json.processor.JsonBuilder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.*


abstract class Publisher(): DefaultTask() {
  @get:Input
  abstract val MBData: Property<DataMineBBS>

  @get:Input
  abstract val Jar: RegularFileProperty

  @TaskAction
  fun run() {
    val data = this.MBData.get()!!
    checker(data, this)

    println("参数检查通过。")
    println("构建完成。欢迎使用 MineBBS 自动推送工具！")

    println("正在准备处理数据。")
    if (data.url.isNotEmpty()) {
      println("提交 Url 模式。")
      updateUrl(data)
      println("大功告成！")
      return
    }

      println("自动任务模式。")
      println("准备上传文件。")
      val fileKey = uploadFile(data.token, this)
      println("准备上传资源。")
      updateFile(data, fileKey)
      println("大功告成！")
  }

  companion object {
    const val URL_BASIC = "https:/api.minebbs.com/api/openapi/v1/"
    val CLIENT = OkHttpClient()

    enum class Action(val node: String) {
      TEST_CONNECTION(URL_BASIC),
      UPLOAD("upload/"),
      UPDATE("resources/%s/update/")
    }

    fun requestTest(): String {
      val request = Request.Builder()
      request.url(URL_BASIC)
      request.get()
      val response = CLIENT.newCall(request.build()).execute()
      if (!(response.isSuccessful)) {
        println(response.code)
        println(response.message)
        throw Error("MineBBS 服务器接口无法连接或过期！")
      }
      val dat = response.body?.string() ?: throw Error("MineBBS 服务器接口无法连接或过期！")
      response.close()

      return dat
    }

    fun requestUpload(token: String, file: File): String {
      val request = Request.Builder()
      request.url(URL_BASIC + Action.UPLOAD.node)
      request.header("Authorization", "Bearer $token")

      val fileBody: RequestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
      val requestBody: RequestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("upload[]", file.getName(), fileBody)
        .build()

      request.post(requestBody)
      val response = CLIENT.newCall(request.build()).execute()
      if (!(response.isSuccessful)) {
        println(response.code)
        println(response.message)
        throw Error("MineBBS 服务器接口无法连接或过期！")
      }

      val bodyOut = response.body?.string() ?: throw Error("MineBBS 服务器接口无法连接或过期！")
      checkReq(bodyOut)
      response.close()

      val node = JSON.json.parse(bodyOut).asMapNodeOrEmpty()
      val array = node.get("data").asArrayNodeOrEmpty()
      if (array.isEmpty) {
        throw Error("文件上传失败！")
      }

      return array.get(0).toString()
    }

    fun requestUpdate(token: String, id: String, node: MapNode) {
      val request = Request.Builder()
      request.url(URL_BASIC + Action.UPDATE.node.format(id))
      request.header("Authorization", "Bearer $token")

      val body = JsonBuilder.asString(node).toRequestBody("application/json;charset=utf-8".toMediaType())
      request.post(body)

      val response = CLIENT.newCall(request.build()).execute()
      if (!(response.isSuccessful)) {
        println(response.code)
        println(response.message)
        throw Error("MineBBS 服务器接口无法连接或过期！")
      }
      response.close()
    }

    fun checkReq(response: String) {
      response.ifEmpty {
        throw Error("未知的请求错误！")
      }

      val node = JSON.json.parse(response).asMapNodeOrEmpty()
      val success = node.getAsTypeOrNull("success", JsonNode.NodeType.Boolean)
      if (success.isNull || !(success.obj as Boolean)) {
        throw Error("MineBBS 开放平台暂不可用！")
      }
    }

    fun checker(data: DataMineBBS, obj: Publisher) {
      if (data.token.isEmpty()) {
        throw IllegalArgumentException("Token 不可为空!")
      }

      if (data.projectId == -1) {
        throw IllegalArgumentException("项目 ID 不可为空或负数!")
      }

      if (data.url.isEmpty() && Objects.isNull(obj.Jar.orNull)) {
        throw IllegalArgumentException("项目打包失败！")
      }

      val out = requestTest()
      checkReq(out)
    }

    fun createUpdateNode(data: DataMineBBS): MapNode {
      val node = MapNode()
      node.put("title", data.title)
      node.put("description", data.description)
      node.put("version", data.version)
      return node
    }

    fun updateUrl(data: DataMineBBS) {
      val node = createUpdateNode(data)
      node.put("file_url", data.url)
      requestUpdate(data.token, data.projectId.toString(), node)
    }

    fun uploadFile(token: String, target: Publisher): String {
      return requestUpload(token, target.Jar.get().asFile)
    }

    fun updateFile(data: DataMineBBS, key: String) {
      val node = createUpdateNode(data)
      node.put("file_key", key)
      requestUpdate(data.token, data.projectId.toString(), node)
    }
  }
}

fun FormBody.Builder.add(pair: Pair<String, String>) {
  this.add(pair.first, pair.second)
}


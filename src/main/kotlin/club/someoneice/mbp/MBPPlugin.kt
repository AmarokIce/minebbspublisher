package club.someoneice.mbp

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar

class MBPPlugin: Plugin<Project> {
  override fun apply(target: Project) {
    val data = target.extensions.create("publisherMineBBS", DataMineBBS::class.java)
    val jarTask = target.tasks.named("jar", Jar::class.java)

    target.tasks.create("publishMineBBS", Publisher::class.java) {
      this.MBData.set(data)

      if (data.url.isNotEmpty()) {
        return@create
      }

      this.dependsOn(jarTask)
      this.Jar.set(jarTask.flatMap { it.archiveFile })
    }
  }
}
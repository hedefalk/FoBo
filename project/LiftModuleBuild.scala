import sbt._
import sbt.Keys._
import java.lang.String
import Unidoc.{ JavaDoc, javadocSettings, junidocSources, sunidoc, unidocExclude }
import annotation.tailrec

object LiftModuleFoBoBuild extends Build {
  
  val liftVersion = SettingKey[String]("liftVersion", "Version number of the Lift Web Framework")
  
  val liftEdition = SettingKey[String]("liftEdition", "Lift Edition (short version number to append to artifact name)")
  
  lazy val root = Project(id   = "FoBo-Meta", 
                             base = file("."),
                             settings = parentSettings ++ Unidoc.settings ++ unidocScaladocSettings ++
                                        inConfig(JavaDoc)(Defaults.configSettings) ++ Seq(
                                            unidocExclude := Seq(foboLess.id),
                                            sources in JavaDoc <<= junidocSources,
                                            javacOptions in JavaDoc := Seq(),
                                            artifactName in packageDoc in JavaDoc := ((sv, mod, art) => "" + mod.name + "_" + sv.binary + "-" + mod.revision + "-javadoc.jar"),
                                            packageDoc in Compile <<= packageDoc in JavaDoc
                                        
                             ),
                             aggregate = Seq(fobo)
                                 )
                            
         
   lazy val fobo        = Project(id = "FoBo",
                            base = file("FoBo"),
                            settings = defaultSettings ++ scaladocSettings ++ Seq(
                                 fullClasspath in doc in Compile <<= fullClasspath in Compile),
                             aggregate = Seq(jquery,bootstrap,fontAwesome,foundation,datatables,knockout,angularjs,jquerymobile,prettify)
                                 ).dependsOn(angularjs,jquery,bootstrap,fontAwesome,foundation,datatables,knockout,jquerymobile,prettify)  
                                 
   lazy val fontAwesome = Project(id   = "FoBo-Font-Awesome", 
                             base = file("Font-Awesome"),
                             settings = defaultSettings ++ scaladocSettings ++ Seq(
                                 fullClasspath in doc in Compile <<= fullClasspath in Compile)
                                 )  
                             
  lazy val bootstrap    = Project(id   = "FoBo-Twitter-Bootstrap", 
                             base = file("Twitter-Bootstrap"),
                             settings = defaultSettings ++ scaladocSettings ++ Seq(
                                 fullClasspath in doc in Compile <<= fullClasspath in Compile))  
                             
  lazy val foundation   = Project(id   = "FoBo-Foundation", 
                             base = file("Foundation"),
                             settings = defaultSettings ++ scaladocSettings ++ Seq(
                                 fullClasspath in doc in Compile <<= fullClasspath in Compile))                                               
                             
  lazy val datatables   = Project(id   = "FoBo-DataTables", 
                             base = file("DataTables"),
                             settings = defaultSettings ++ scaladocSettings ++ Seq(
                                 fullClasspath in doc in Compile <<= fullClasspath in Compile))                  
                             
  lazy val knockout     = Project(id   = "FoBo-Knockout", 
                             base = file("Knockout"),
                             settings = defaultSettings ++ scaladocSettings ++ Seq(
                                 fullClasspath in doc in Compile <<= fullClasspath in Compile)) 
                   
  lazy val angularjs    = Project(id   = "FoBo-AngularJS", 
                             base = file("AngularJS"),
                             settings = defaultSettings ++ scaladocSettings ++ Seq(
                                 fullClasspath in doc in Compile <<= fullClasspath in Compile))  
                             
  lazy val jquery       = Project(id   = "FoBo-JQuery", 
                             base = file("JQuery"),
                             settings = defaultSettings ++ scaladocSettings ++ Seq(
                                 fullClasspath in doc in Compile <<= fullClasspath in Compile))  
                             
  lazy val jquerymobile = Project(id   = "FoBo-JQuery-Mobile", 
                             base = file("JQuery-Mobile"),
                             settings = defaultSettings ++ scaladocSettings ++ Seq(
                                 fullClasspath in doc in Compile <<= fullClasspath in Compile))  
                             
  lazy val prettify     = Project(id   = "FoBo-Google-Code-Prettify", 
                             base = file("Google-Code-Prettify"),
                             settings = defaultSettings ++ scaladocSettings ++ Seq(
                                 fullClasspath in doc in Compile <<= fullClasspath in Compile))                               
                             
  lazy val foboLess     = Project(id   = "FoBo-Less", 
                             base = file("FoBo-Less"))     
                             
                             
                       
                             

  lazy val scaladocDiagramsEnabled = System.getProperty("scaladoc.diagrams", "false").toBoolean
  lazy val scaladocOptions = List() /*List("-implicits")*/ ::: (if (scaladocDiagramsEnabled) List("-diagrams") else Nil)

  lazy val scaladocSettings: Seq[sbt.Setting[_]]= {
    Seq(scalacOptions in (Compile, doc) ++= scaladocOptions) ++
      (if (scaladocDiagramsEnabled)
        Seq(doc in Compile ~= scaladocVerifier)
       else Seq.empty)
  }

  lazy val unidocScaladocSettings: Seq[sbt.Setting[_]]= {
    Seq(scalacOptions in doc ++= scaladocOptions) ++
      (if (scaladocDiagramsEnabled)
        Seq(sunidoc ~= scaladocVerifier)
      else Seq.empty)
  }

  def scaladocVerifier(file: File): File= {
    @tailrec
    def findHTMLFileWithDiagram(dirs: Seq[File]): Boolean = {
      if (dirs.isEmpty) false
      else {
        val curr = dirs.head
        val (newDirs, files) = curr.listFiles.partition(_.isDirectory)
        val rest = dirs.tail ++ newDirs
        val hasDiagram = files exists { f =>
          val name = f.getName
          if (name.endsWith(".html") && !name.startsWith("index-") &&
            !(name.compare("index.html") == 0) && !(name.compare("package.html") == 0)) {
            val source = scala.io.Source.fromFile(f)
            val hd = source.getLines().exists(_.contains("<div class=\"toggleContainer block diagram-container\" id=\"inheritance-diagram-container\">"))
            source.close()
            hd
          }
          else false
        }
        hasDiagram || findHTMLFileWithDiagram(rest)
      }
    }

    // if we have generated scaladoc and none of the files have a diagram then fail
    if (file.exists() && !findHTMLFileWithDiagram(List(file)))
      sys.error("ScalaDoc diagrams not generated!")
    else
      file
  }                             

  lazy val baseSettings = Defaults.defaultSettings 

  lazy val parentSettings = baseSettings ++ Seq(
    publishArtifact in Compile := false
  )
  
  lazy val defaultSettings = baseSettings ++ Seq(description := "FoBo", parallelExecution in Test := false)  
  
}

object Dependencies {
  object Compile { 
    
    // Compiler plugins
    val genjavadoc = compilerPlugin("com.typesafe.genjavadoc" %% "genjavadoc-plugin" % "0.4" cross CrossVersion.full)  
  }
  import Compile._
}



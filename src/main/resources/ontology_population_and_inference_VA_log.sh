pwsh -command "$Host.UI.RawUI.WindowTitle = 'ontology_population_and_inference_VA.log'; $pshost = Get-Host ; $psWindow = $pshost.UI.RawUI ;  $newSize = $psWindow.WindowSize ;  $newSize.Height = 27 ; $newSize.Width =172; $psWindow.WindowSize= $newSize ; add-type -AssemblyName microsoft.VisualBasic ; add-type -AssemblyName System.Windows.Forms ; [System.Windows.Forms.SendKeys]::SendWait('~'); get-content /home/kass/Desktop/suitceyes/suitceyes/src/main/resources/logOntologyPopulationAndInferenceVA.log -wait -tail 10"
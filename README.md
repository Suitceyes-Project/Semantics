# Semantics
The ontology and the JAVA program that handles the information coming from various sensors and the user, the inference done using SPIN RULES, and the outcome that can be seen on the web interface.

# Description
This is the repository for the Suitceyes-Semantics module. In the Semantics repository, the incoming messages from the VA are processed and the information detected (objects, distance, people, etc.) is populated in the ontology that is stored in a GraphDB repo. Whenever the user sends a query via the haptic device, the JAVA application performs a process called inference usins SPIN/SPARQL rules to answer the user query. The query answeer is then sent back to the user via the message bus, in the form of a json.

# Requirements
The Semantics application is developed in JAVA version 11.
Additional requirements that will have to be intalled or have an account created, are also listed.

Ably framework: A a pub/sub messaging broker for web and mobile apps. Account is required.  
GraphDB repo: GraphDB is an enterprise ready Semantic Graph Database, compliant with W3C Standards. Further instructions on how to install GraphDB can be found in instructionsGrabhDBSuitceyes.docx.  

# Contact
For further details and for access to the developed models, please contact Vasileios Kassiano (vaskass@iti.gr).

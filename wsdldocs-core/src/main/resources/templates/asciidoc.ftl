<#if document??>
= ${document.title}
:toc:
:toclevels: 3
:icons: font
:doctype: book
:pdf-page-size: A4

:numbered:

<#if document.description??>
== WebService Description

${document.description}
</#if>

<#if document.locations??>

== Locations

<#list document.locations as locName, loc>

=== ${locName}

endpoint:: `${loc.endpoint}`

wsdl:: `<#if loc.wsdl??>${loc.wsdl}<#else>${loc.endpoint}.wsdl</#if>`
</#list>
</#if>
</#if>

<#list wsdl.portTypes as portType>
== Port: `${portType.name}`

=== Operations

<#list portType.operations as operation>
[[operation_${operation.name}]]
==== `${operation.name}`
[horizontal]
Description:: ${operation.documentation!"-"}

Input:: `${operation.input.message.part.name}`
+
----
${operation.input.message.part.rawSchemaType}
----

<#if operation.hasOutput()>
Output:: `${operation.output.message.part.name}`
+
----
${operation.output.message.part.rawSchemaType}
----
</#if>

<#if operation.hasFaults()>
Faults:: <#list operation.faults as fault>`xref:fault_${fault.message.part.name}[${fault.message.part.name}]` </#list>
</#if>

</#list>
</#list>

== Faults

<#list wsdl.allFaults as fault>

[horizontal]
[[fault_${fault.name}]]
`${fault.message.part.name}`::
+
----
${fault.message.part.rawSchemaType}
----

</#list>
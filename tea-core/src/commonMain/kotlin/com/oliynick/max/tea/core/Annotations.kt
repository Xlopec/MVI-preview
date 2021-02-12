package com.oliynick.max.tea.core

/**
 * Marks declarations that are **obsolete** in Component API, which means that the design of the corresponding
 * declarations has serious known flaws and they will be redesigned in the future.
 * Roughly speaking, these declarations will be deprecated in the future but there is no replacement for them yet,
 * so they cannot be deprecated right away.
 */
@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
annotation class ObsoleteComponentApi

/**
 * Marks declarations that are **unstable** in Component API, which means that the design of the corresponding
 * declarations isn't stable and might be changed in the future.
 */
@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(AnnotationTarget.TYPEALIAS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
annotation class UnstableApi

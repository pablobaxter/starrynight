/*
 * Starry Nights - A BlueSky Android Client
 * Copyright (C) 2026 pablo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.frybits.gradle.atproto.fakes

import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import java.io.File

internal class FakeDirectory: Directory {
    override fun getAsFile(): File {
        return File.createTempFile("test", "fake")
    }

    override fun getAsFileTree(): FileTree {
        TODO("Not yet implemented")
    }

    override fun dir(path: String): Directory {
        TODO("Not yet implemented")
    }

    override fun dir(path: Provider<out CharSequence>): Provider<Directory> {
        TODO("Not yet implemented")
    }

    override fun file(path: String): RegularFile {
        TODO("Not yet implemented")
    }

    override fun file(path: Provider<out CharSequence>): Provider<RegularFile> {
        TODO("Not yet implemented")
    }

    override fun files(vararg paths: Any?): FileCollection {
        TODO("Not yet implemented")
    }
}

package com.eriktrummal.scalefinder.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.eriktrummal.scalefinder.MainViewModel
import com.eriktrummal.scalefinder.R
import com.eriktrummal.scalefinder.data.Scale
import com.eriktrummal.scalefinder.data.ScaleItem
import com.eriktrummal.scalefinder.ui.components.BackgroundImage
import com.eriktrummal.scalefinder.ui.components.PianoView

@Composable
fun ScaleFinder(viewModel: MainViewModel, navController: NavController) {
    val selectedNotes by viewModel.selectedNotes.collectAsState()
    val rootNote by viewModel.rootNote.collectAsState()
    val useFlats by viewModel.useFlats.collectAsState()
    val familyInclusionUpdateTrigger by viewModel.familyInclusionUpdateTrigger.collectAsState()
    var matchingScales by remember { mutableStateOf(listOf<Scale>()) }
    var potentialMatchingScales by remember { mutableStateOf(List(12) { emptyList<Scale>() }) }
    var hidePiano by remember { mutableStateOf(false) }

    val selectedNoteNames by remember(selectedNotes, useFlats) {
        derivedStateOf {
            selectedNotes.map { noteNumber ->
                getNoteNameFromNumber(noteNumber, useFlats)
            }
        }
    }

    val potentialMatchCounts by remember(potentialMatchingScales) {
        derivedStateOf {
            potentialMatchingScales.map { it.size }
        }
    }

    LaunchedEffect(selectedNotes, rootNote, familyInclusionUpdateTrigger) {
        matchingScales = viewModel.getMatchingScales(selectedNotes, rootNote)
        potentialMatchingScales = viewModel.getPotentialMatchingScales(selectedNotes, rootNote)
    }

    BackgroundImage(
        backgroundImageRes = R.drawable.bg5,
        backgroundTint = colorResource(id = R.color.background_image_tint),
        backgroundOpacity = 0.6f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResetButton(
                    onClick = {
                        viewModel.setSelectedNotes(emptyList())
                        viewModel.setRootNote(null)
                        matchingScales = emptyList()
                        potentialMatchingScales = List(12) { emptyList() }
                    },
                    modifier = Modifier.weight(1f)
                )
                SharpFlatToggle(
                    useFlats = useFlats,
                    onToggle = { viewModel.setUseFlats(it) },
                    modifier = Modifier.weight(1f)
                )
                PianoToggleButton(
                    hidePiano = hidePiano,
                    onToggle = { hidePiano = !hidePiano },
                    modifier = Modifier.weight(1f)
                )
            }

            if (!hidePiano) {
                PianoView(
                    selectedNotes = selectedNotes,
                    noteNames = selectedNoteNames,
                    rootNote = rootNote,
                    potentialMatchCounts = potentialMatchCounts,
                    onNoteSelected = { note ->
                        if (note == rootNote) {
                            viewModel.setRootNote(null)
                        }
                        viewModel.setSelectedNotes(
                            if (selectedNotes.contains(note)) {
                                selectedNotes - note
                            } else {
                                selectedNotes + note
                            }
                        )
                    },
                    onNoteLongPress = { note ->
                        viewModel.setRootNote(if (rootNote == note) null else note)
                        if (!selectedNotes.contains(note)) {
                            viewModel.setSelectedNotes(selectedNotes + note)
                        }
                    },
                    fontSize = 40f
                )

                if (selectedNotes.isNotEmpty()) {
                    if (matchingScales.isEmpty()) {
                        Text(
                            text = "There aren't any scales that contain these notes!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                    if (selectedNotes.size == 1) {
                        Text(
                            text = "${matchingScales.size} different scales contain this note.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Keep adding more notes!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    } else {
                        if (matchingScales.size == 1) {
                            Text(
                                text = "There is only one scale that contains these notes:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "${matchingScales.size} different scales contain these notes:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            if (selectedNotes.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 32.dp)
                ) {
                    items(matchingScales) { scale ->
                        ScaleItem(
                            scale = scale,
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 56.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Press on any of the keys on the piano to display all the different scales that contain that note!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Set the root note of the scale by doing a long press!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "The numbers on all of the keys show how many different scales there are that contain the notes you've selected.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Check the side menu to disable any scales that you don't want to show up.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

fun getNoteNameFromNumber(noteNumber: Int, useFlats: Boolean): String {
    val sharpNotes = listOf("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")
    val flatNotes = listOf("C", "D♭", "D", "E♭", "E", "F", "G♭", "G", "A♭", "A", "B♭", "B")

    val notes = if (useFlats) flatNotes else sharpNotes
    return notes[((noteNumber - 1) % 12 + 12) % 12]
}

@Composable
fun PianoToggleButton(
    hidePiano: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = colorResource(id = R.color.root_white_key_tint)
    val iconColor = colorResource(id = R.color.key_shadow)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onToggle)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (hidePiano) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = if (hidePiano) "Show Piano" else "Hide Piano",
            tint = iconColor
        )
    }
}

@Composable
fun ResetButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = colorResource(id = R.color.reset_button)
    val textColor = colorResource(id = R.color.bottom_bar_unselected_item)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Reset",
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SharpFlatToggle(
    useFlats: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = colorResource(id = R.color.bottom_bar_fill_color_1)
    val selectedColor = colorResource(id = R.color.sharps_flats_toggle_button_selection)
    val textColor = colorResource(id = R.color.top_bar_text)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (!useFlats) selectedColor else Color.Transparent)
                    .clickable { onToggle(false) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "♯",
                    color = if (!useFlats) Color.White else textColor
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (useFlats) selectedColor else Color.Transparent)
                    .clickable { onToggle(true) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "♭",
                    color = if (useFlats) Color.White else textColor
                )
            }
        }
    }
}
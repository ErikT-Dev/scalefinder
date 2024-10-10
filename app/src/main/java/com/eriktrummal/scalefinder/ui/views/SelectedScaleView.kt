package com.eriktrummal.scalefinder.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import com.eriktrummal.scalefinder.MainViewModel
import com.eriktrummal.scalefinder.R
import com.eriktrummal.scalefinder.data.Scale
import com.eriktrummal.scalefinder.ui.components.BackgroundImage
import com.eriktrummal.scalefinder.ui.components.PianoView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedScaleView(scaleId: Int, viewModel: MainViewModel, navController: NavController) {
    var scale by remember { mutableStateOf<Scale?>(null) }
    val myScalesStatus by viewModel.myScalesStatus.collectAsState()
    val isInMyScales = myScalesStatus[scaleId] ?: false
    var selectedChord by remember { mutableStateOf<ChordItemData?>(null) }
    var showTriads by remember { mutableStateOf(true) }

    LaunchedEffect(scaleId) {
        viewModel.checkScaleInMyScales(scaleId)
        scale = viewModel.getScaleById(scaleId) ?: viewModel.getScaleByIdFromDatabase(scaleId)
    }

    BackgroundImage(
        backgroundImageRes = R.drawable.bg4,
        backgroundTint = colorResource(id = R.color.background_image_tint),
        backgroundOpacity = 0.6f
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Back", style = MaterialTheme.typography.titleMedium, color = Color.LightGray) },
                    modifier = Modifier.height(50.dp),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                scale?.let {
                                    if (isInMyScales) {
                                        viewModel.removeFromMyScales(it)
                                    } else {
                                        viewModel.addToMyScales(it)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = if (isInMyScales) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24),
                                contentDescription = if (isInMyScales) "Remove from MyScales" else "Add to MyScales",
                                tint = colorResource(id = R.color.heart_icon_button),
                            )
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            if (scale != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = scale!!.formattedFullName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                    item {
                        Text(
                            text = "Listed under: " + scale!!.family,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Normal,
                            color = Color.LightGray
                        )
                    }
                    item {
                        PianoView(
                            selectedNotes = scale!!.notes,
                            noteNames = scale!!.noteNames,
                            chordNotes = selectedChord?.chordNoteNumbers ?: emptyList(),
                            rootNote = scale!!.root,
                            fontSize = 40f
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SegmentedButton(
                                options = listOf("Triads", "Sevenths"),
                                selectedOption = if (showTriads) "Triads" else "Sevenths",
                                onOptionSelected = { option ->
                                    showTriads = option == "Triads"
                                }
                            )
                        }
                    }

                    val allChordsOfScale = if (showTriads) {
                        generateTriadChords(scale!!.notes, scale!!.noteNames)
                    } else {
                        generateSeventhChords(scale!!.notes, scale!!.noteNames)
                    }

                    if (allChordsOfScale.isNotEmpty()) {
                        item {
                            Text(
                                text = "The following ${if (showTriads) "triads" else "tertian seventh chords"} can be built from this scale:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                        item {
                            TwoColumnLayout(
                                items = allChordsOfScale,
                                selectedChord = selectedChord,
                                onChordSelected = { clickedChord ->
                                    selectedChord = if (selectedChord == clickedChord) null else clickedChord
                                }
                            )
                        }
                    }
                }
            } else {
                Text("Scale not found", modifier = Modifier.padding(16.dp), color = Color.White)
            }
        }
    }
}

@Composable
fun SegmentedButton(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small)
            .padding(4.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption
            Button(
                onClick = { onOptionSelected(option) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f)
            ) {
                Text(option)
            }
            if (index < options.size - 1) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

fun generateTriadChords(scaleNotes: List<Int>, scaleNoteNames: List<String>): List<ChordItemData> {
    val chords = mutableListOf<ChordItemData>()

    for (i in scaleNotes.indices) {
        val root = scaleNotes[i]
        val rootName = scaleNoteNames[i]

        val chordTypes = listOf(
            Triple("maj", 4, 7),
            Triple("min", 3, 7),
            Triple("dim", 3, 6),
            Triple("aug", 4, 8)
        )

        for ((chordType, thirdInterval, fifthInterval) in chordTypes) {
            val thirdIndex = scaleNotes.indexOfFirst { (it - root + 12) % 12 == thirdInterval }
            val fifthIndex = scaleNotes.indexOfFirst { (it - root + 12) % 12 == fifthInterval }

            if (thirdIndex != -1 && fifthIndex != -1) {
                val third = scaleNotes[thirdIndex]
                val fifth = scaleNotes[fifthIndex]
                val thirdName = scaleNoteNames[thirdIndex]
                val fifthName = scaleNoteNames[fifthIndex]

                val chordName = "$rootName $chordType"
                val chordNotes = listOf(rootName, thirdName, fifthName)
                val chordNoteNumbers = listOf(root, third, fifth)

                chords.add(ChordItemData(chordName, chordNotes, chordNoteNumbers))
            }
        }
    }
    return chords
}

fun generateSeventhChords(scaleNotes: List<Int>, scaleNoteNames: List<String>): List<ChordItemData> {
    val chords = mutableListOf<ChordItemData>()

    for (i in scaleNotes.indices) {
        val root = scaleNotes[i]
        val rootName = scaleNoteNames[i]

        val chordTypes = listOf(
            Pair("maj7", listOf(4, 7, 11)),
            Pair("min7", listOf(3, 7, 10)),
            Pair("dom7", listOf(4, 7, 10)),
            Pair("dim7", listOf(3, 6, 9)),
            Pair("half-dim7", listOf(3, 6, 10)),
            Pair("min-maj7", listOf(3, 7, 11)),
            Pair("aug-maj7", listOf(4, 8, 11))
        )

        for ((chordType, intervals) in chordTypes) {
            val chordNoteIndices = intervals.map { interval ->
                scaleNotes.indexOfFirst { (it - root + 12) % 12 == interval }
            }

            if (chordNoteIndices.all { it != -1 }) {
                val chordNotes = chordNoteIndices.map { scaleNotes[it] }
                val chordNoteNames = chordNoteIndices.map { scaleNoteNames[it] }

                val chordName = "$rootName $chordType"
                val allChordNotes = listOf(rootName) + chordNoteNames
                val allChordNoteNumbers = listOf(root) + chordNotes

                chords.add(ChordItemData(chordName, allChordNotes, allChordNoteNumbers))
            }
        }
    }
    return chords
}

@Composable
fun TwoColumnLayout(
    items: List<ChordItemData>,
    selectedChord: ChordItemData?,
    onChordSelected: (ChordItemData) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        ChordItem(
                            chordData = item,
                            isSelected = item == selectedChord,
                            onClick = { onChordSelected(item) }
                        )
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

data class ChordItemData(
    val chordName: String,
    val chordNotes: List<String>,
    val chordNoteNumbers: List<Int>
)

@Composable
fun ChordItem(
    chordData: ChordItemData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                colorResource(id = R.color.white_chord_note_key_tint).copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chordData.chordName,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) Color.White else Color.DarkGray
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                chordData.chordNotes.forEach { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) Color.White else Color.DarkGray
                    )
                }
            }
        }
    }
}